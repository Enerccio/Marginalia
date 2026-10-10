package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.service.CleanupService;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.metamodel.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class CleanupServiceImpl implements CleanupService {

    private static final Logger log = LoggerFactory.getLogger(CleanupServiceImpl.class);

    private static final int CHUNK_SIZE = 500;
    private static final int REMOVE_BATCH_SIZE = 200;
    private static final int MAX_REASONS = 5;
    private static final String[] LABEL_METHODS = {"getName", "getLogin", "getValue"};

    private final ExtendableEntityListener extendableEntityListener = new ExtendableEntityListener();
    private final List<CleanupContributor> contributors = new CopyOnWriteArrayList<>();

    @Autowired
    private AdminGuard adminGuard;

    private EntityManager entityManager;
    private volatile Model model;

    @Override
    @CommonTxReadOnly
    public List<ReferenceDescriptor> getReferenceModel() throws Exception {
        adminGuard.requireAdmin();
        return Collections.unmodifiableList(getModel().references);
    }

    @Override
    @CommonTxReadOnly
    public CleanupPlan analyze() throws Exception {
        adminGuard.requireAdmin();
        CleanupPlan plan = computePlan(getModel());
        for (BlockedEntity blocked : plan.getBlocked().values()) {
            blocked.setLabel(label(blocked.getKey()));
        }
        return plan;
    }

    @Override
    @CommonTx
    public CleanupPlan purge() throws Exception {
        adminGuard.requireAdmin();
        Model m = getModel();
        CleanupPlan plan = computePlan(m);
        Set<EntityKey> purge = Collections.unmodifiableSet(plan.getPurge());
        if (!purge.isEmpty()) {
            clearWeakReferences(m, purge);
            for (CleanupContributor contributor : contributors) {
                contributor.beforePurge(purge);
            }
            entityManager.flush();
            entityManager.clear();
            detachReferences(m, purge);
            remove(purge);
        }
        plan.setExecuted(true);
        log.info("Cleanup purged {} entities, {} soft deleted entities blocked", purge.size(), plan.getBlocked().size());
        return plan;
    }

    @Override
    public void registerContributor(CleanupContributor contributor) {
        contributors.add(contributor);
    }

    @Override
    public void unregisterContributor(CleanupContributor contributor) {
        contributors.remove(contributor);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // reference model
    // ---------------------------------------------------------------------------------------------------------------

    private static class Model {
        private final List<ReferenceDescriptor> references = new ArrayList<>();
        private final Map<Class<?>, String> rootEntities = new LinkedHashMap<>();
        private final Map<String, Class<?>> classNameToRoot = new HashMap<>();
    }

    private Model getModel() {
        if (model == null) {
            synchronized (this) {
                if (model == null) {
                    model = buildModel();
                }
            }
        }
        return model;
    }

    private Model buildModel() {
        Model m = new Model();
        Metamodel metamodel = entityManager.getMetamodel();

        for (EntityType<?> entityType : metamodel.getEntities()) {
            EntityType<?> root = rootOf(entityType);
            m.classNameToRoot.put(entityType.getJavaType().getName(), root.getJavaType());
            if (root == entityType && BaseEntity.class.isAssignableFrom(root.getJavaType())) {
                m.rootEntities.put(root.getJavaType(), root.getName());
            }
        }

        for (EntityType<?> entityType : metamodel.getEntities()) {
            boolean isRoot = rootOf(entityType) == entityType;
            Class<?> referrerType = rootOf(entityType).getJavaType();
            // root sees inherited mapped superclass attributes, subclasses only add their own
            Set<? extends Attribute<?, ?>> attributes = isRoot ? entityType.getAttributes() : entityType.getDeclaredAttributes();
            Map<String, Attribute<?, ?>> attributesByName = new HashMap<>();

            for (Attribute<?, ?> attribute : attributes) {
                attributesByName.put(attribute.getName(), attribute);
                if (!attribute.isAssociation()) {
                    continue;
                }
                Field field = attribute.getJavaMember() instanceof Field f ? f : null;
                if (field != null && isInverseSide(field)) {
                    continue;
                }
                Class<?> targetJavaType = attribute instanceof PluralAttribute<?, ?, ?> plural
                        ? plural.getElementType().getJavaType()
                        : ((SingularAttribute<?, ?>) attribute).getType().getJavaType();
                Class<?> targetType = m.classNameToRoot.get(targetJavaType.getName());
                if (targetType == null) {
                    continue;
                }
                m.references.add(new ReferenceDescriptor(entityType.getName(), referrerType, attribute.getName(),
                        targetType, null, resolvePolicy(entityType.getJavaType(), attribute.getName(), field),
                        true, attribute.isCollection(), true));
            }

            for (Field field : getFields(entityType.getJavaType(), isRoot)) {
                CleanupReference annotation = field.getAnnotation(CleanupReference.class);
                Attribute<?, ?> attribute = attributesByName.get(field.getName());
                if (annotation == null || (attribute != null && attribute.isAssociation())) {
                    continue;
                }
                Class<?> targetType = annotation.target() != BaseEntity.class ? m.classNameToRoot.get(annotation.target().getName()) : null;
                String targetClassField = StringUtils.trimToNull(annotation.targetClassField());
                if (targetType == null && targetClassField == null) {
                    log.warn("Soft reference {}.{} has neither known target nor target class field, ignoring", entityType.getName(), field.getName());
                    continue;
                }
                m.references.add(new ReferenceDescriptor(entityType.getName(), referrerType, field.getName(),
                        targetType, targetClassField, annotation.value(), false, false, attribute != null));
            }
        }

        log.debug("Cleanup reference model: {}", m.references);
        return m;
    }

    private EntityType<?> rootOf(EntityType<?> entityType) {
        IdentifiableType<?> type = entityType;
        while (type.getSupertype() instanceof EntityType<?> superType) {
            type = superType;
        }
        return (EntityType<?>) type;
    }

    private boolean isInverseSide(Field field) {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        if (oneToMany != null && StringUtils.isNotBlank(oneToMany.mappedBy())) {
            return true;
        }
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        if (oneToOne != null && StringUtils.isNotBlank(oneToOne.mappedBy())) {
            return true;
        }
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        return manyToMany != null && StringUtils.isNotBlank(manyToMany.mappedBy());
    }

    private Policy resolvePolicy(Class<?> entityClass, String fieldName, Field field) {
        if (field != null && field.getAnnotation(CleanupReference.class) != null) {
            return field.getAnnotation(CleanupReference.class).value();
        }
        for (Class<?> cls = entityClass; cls != null; cls = cls.getSuperclass()) {
            for (CleanupReference annotation : cls.getAnnotationsByType(CleanupReference.class)) {
                if (fieldName.equals(annotation.field())) {
                    return annotation.value();
                }
            }
        }
        return Policy.STRONG;
    }

    private List<Field> getFields(Class<?> entityClass, boolean includeSuperclasses) {
        List<Field> fields = new ArrayList<>(Arrays.asList(entityClass.getDeclaredFields()));
        if (includeSuperclasses) {
            for (Class<?> cls = entityClass.getSuperclass(); cls != null && cls != Object.class; cls = cls.getSuperclass()) {
                fields.addAll(Arrays.asList(cls.getDeclaredFields()));
            }
        }
        return fields;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // planning
    // ---------------------------------------------------------------------------------------------------------------

    private record Edge(EntityKey referrer, EntityKey target, ReferenceDescriptor descriptor) {
    }

    private static class Closure {
        private final Set<EntityKey> members = new LinkedHashSet<>();
        // members that must be purged for their origins to be purged (reached only via OWNED_BY)
        private final Set<EntityKey> mandatory = new HashSet<>();
        private final Map<EntityKey, Set<EntityKey>> origins = new HashMap<>();
    }

    private CleanupPlan computePlan(Model m) throws Exception {
        Set<EntityKey> softDeleted = new LinkedHashSet<>();
        for (Map.Entry<Class<?>, String> root : m.rootEntities.entrySet()) {
            List<Long> ids = entityManager.createQuery("SELECT e.id FROM " + root.getValue() + " e WHERE e.deleted = true", Long.class)
                    .getResultList();
            for (Long id : ids) {
                softDeleted.add(new EntityKey(root.getKey(), id));
            }
        }

        Set<EntityKey> blockedRoots = new HashSet<>();
        Set<EntityKey> excluded = new HashSet<>();
        Map<EntityKey, List<String>> reasons = new LinkedHashMap<>();
        Closure closure;

        while (true) {
            Set<EntityKey> roots = new LinkedHashSet<>(softDeleted);
            roots.removeAll(blockedRoots);
            closure = closure(m, roots, excluded);

            boolean changed = false;
            for (Map.Entry<EntityKey, List<String>> blocked : findBlocked(m, closure.members).entrySet()) {
                EntityKey node = blocked.getKey();
                if (closure.mandatory.contains(node)) {
                    for (EntityKey root : closure.origins.getOrDefault(node, Set.of(node))) {
                        changed |= blockedRoots.add(root);
                        List<String> rootReasons = reasons.computeIfAbsent(root, k -> new ArrayList<>());
                        for (String reason : blocked.getValue()) {
                            if (rootReasons.size() < MAX_REASONS) {
                                rootReasons.add(node.equals(root) ? reason : node + ": " + reason);
                            }
                        }
                    }
                } else {
                    changed |= excluded.add(node);
                }
            }
            if (!changed) {
                break;
            }
        }

        CleanupPlan plan = new CleanupPlan();
        plan.getPurge().addAll(closure.members);
        for (EntityKey key : softDeleted) {
            plan.getStats().computeIfAbsent(key.type(), EntityStats::new).setSoftDeleted(
                    plan.getStats().get(key.type()).getSoftDeleted() + 1);
        }
        for (EntityKey key : closure.members) {
            EntityStats stats = plan.getStats().computeIfAbsent(key.type(), EntityStats::new);
            stats.setPurgeable(stats.getPurgeable() + 1);
        }
        for (EntityKey key : blockedRoots) {
            EntityStats stats = plan.getStats().computeIfAbsent(key.type(), EntityStats::new);
            stats.setBlocked(stats.getBlocked() + 1);
            BlockedEntity blockedEntity = new BlockedEntity(key);
            blockedEntity.getReasons().addAll(reasons.getOrDefault(key, List.of()));
            plan.getBlocked().put(key, blockedEntity);
        }
        return plan;
    }

    private Closure closure(Model m, Set<EntityKey> roots, Set<EntityKey> excluded) throws Exception {
        Closure closure = new Closure();
        for (EntityKey root : roots) {
            closure.members.add(root);
            closure.mandatory.add(root);
            closure.origins.put(root, new HashSet<>(Set.of(root)));
        }

        Set<EntityKey> frontier = new LinkedHashSet<>(roots);
        while (!frontier.isEmpty()) {
            Set<EntityKey> next = new LinkedHashSet<>();
            Map<Class<?>, List<Long>> grouped = group(frontier);
            for (ReferenceDescriptor descriptor : m.references) {
                if (descriptor.getPolicy() == Policy.OWNED_BY) {
                    for (Map.Entry<Class<?>, List<Long>> group : grouped.entrySet()) {
                        for (Edge edge : edgesByTarget(m, descriptor, group.getKey(), group.getValue())) {
                            propagate(closure, edge.referrer(), edge.target(), true, excluded, next);
                        }
                    }
                } else if (descriptor.getPolicy() == Policy.OWNS) {
                    List<Long> ids = grouped.get(descriptor.getReferrerType());
                    if (ids != null) {
                        for (Edge edge : edgesByReferrer(m, descriptor, ids)) {
                            propagate(closure, edge.target(), edge.referrer(), false, excluded, next);
                        }
                    }
                }
            }
            frontier = next;
        }
        return closure;
    }

    private void propagate(Closure closure, EntityKey child, EntityKey from, boolean ownedBy, Set<EntityKey> excluded, Set<EntityKey> next) {
        if (excluded.contains(child)) {
            return;
        }
        boolean changed = closure.members.add(child);
        if (ownedBy && closure.mandatory.contains(from)) {
            changed |= closure.mandatory.add(child);
            changed |= closure.origins.computeIfAbsent(child, k -> new HashSet<>()).addAll(closure.origins.get(from));
        }
        if (changed) {
            next.add(child);
        }
    }

    private Map<EntityKey, List<String>> findBlocked(Model m, Set<EntityKey> members) throws Exception {
        Map<EntityKey, List<String>> blocked = new LinkedHashMap<>();
        Map<Class<?>, List<Long>> grouped = group(members);
        for (ReferenceDescriptor descriptor : m.references) {
            if (descriptor.getPolicy() == Policy.WEAK) {
                continue;
            }
            for (Map.Entry<Class<?>, List<Long>> group : grouped.entrySet()) {
                for (Edge edge : edgesByTarget(m, descriptor, group.getKey(), group.getValue())) {
                    if (!members.contains(edge.referrer())) {
                        blocked.computeIfAbsent(edge.target(), k -> new ArrayList<>())
                                .add(edge.referrer() + " (" + descriptor.getField() + ")");
                    }
                }
            }
        }
        Set<EntityKey> candidates = Collections.unmodifiableSet(members);
        for (CleanupContributor contributor : contributors) {
            contributor.collectReferences(candidates, (target, reason) -> {
                if (members.contains(target)) {
                    blocked.computeIfAbsent(target, k -> new ArrayList<>()).add(reason);
                }
            });
        }
        return blocked;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // reference queries
    // ---------------------------------------------------------------------------------------------------------------

    private List<Edge> edgesByTarget(Model m, ReferenceDescriptor descriptor, Class<?> targetType, List<Long> ids) throws Exception {
        if (descriptor.getTargetType() != null && descriptor.getTargetType() != targetType) {
            return List.of();
        }
        List<Edge> edges = new ArrayList<>();
        if (!descriptor.isPersistent()) {
            Set<Long> idSet = new HashSet<>(ids);
            for (Object entity : entityManager.createQuery("SELECT e FROM " + descriptor.getReferrerEntity() + " e", Object.class).getResultList()) {
                EntityKey target = readSoftReference(m, descriptor, entity);
                if (target != null && target.type() == targetType && idSet.contains(target.id())) {
                    edges.add(new Edge(new EntityKey(descriptor.getReferrerType(), ((BaseEntity) entity).getId()), target, descriptor));
                }
            }
            return edges;
        }
        String targetExpression = targetExpression(descriptor);
        for (List<Long> chunk : chunks(ids)) {
            List<Object[]> rows = entityManager.createQuery(selectClause(descriptor) + " WHERE " + targetExpression + " IN :ids", Object[].class)
                    .setParameter("ids", chunk)
                    .getResultList();
            addEdges(m, descriptor, rows, edges, targetType);
        }
        return edges;
    }

    private List<Edge> edgesByReferrer(Model m, ReferenceDescriptor descriptor, List<Long> referrerIds) throws Exception {
        List<Edge> edges = new ArrayList<>();
        if (!descriptor.isPersistent()) {
            for (List<Long> chunk : chunks(referrerIds)) {
                for (Object entity : entityManager.createQuery("SELECT e FROM " + descriptor.getReferrerEntity() + " e WHERE e.id IN :ids", Object.class)
                        .setParameter("ids", chunk).getResultList()) {
                    EntityKey target = readSoftReference(m, descriptor, entity);
                    if (target != null) {
                        edges.add(new Edge(new EntityKey(descriptor.getReferrerType(), ((BaseEntity) entity).getId()), target, descriptor));
                    }
                }
            }
            return edges;
        }
        String targetExpression = targetExpression(descriptor);
        for (List<Long> chunk : chunks(referrerIds)) {
            List<Object[]> rows = entityManager.createQuery(selectClause(descriptor) + " WHERE e.id IN :ids AND " + targetExpression + " IS NOT NULL", Object[].class)
                    .setParameter("ids", chunk)
                    .getResultList();
            addEdges(m, descriptor, rows, edges, null);
        }
        return edges;
    }

    private String targetExpression(ReferenceDescriptor descriptor) {
        if (descriptor.isCollection()) {
            return "x.id";
        }
        return descriptor.isAssociation() ? "e." + descriptor.getField() + ".id" : "e." + descriptor.getField();
    }

    private String selectClause(ReferenceDescriptor descriptor) {
        String select = "SELECT e.id, " + targetExpression(descriptor);
        if (descriptor.getTargetClassField() != null) {
            select += ", e." + descriptor.getTargetClassField();
        }
        select += " FROM " + descriptor.getReferrerEntity() + " e";
        if (descriptor.isCollection()) {
            select += " JOIN e." + descriptor.getField() + " x";
        }
        return select;
    }

    private void addEdges(Model m, ReferenceDescriptor descriptor, List<Object[]> rows, List<Edge> edges, Class<?> targetTypeFilter) {
        for (Object[] row : rows) {
            if (!(row[1] instanceof Number targetId)) {
                continue;
            }
            Class<?> targetType = descriptor.getTargetType();
            if (descriptor.getTargetClassField() != null) {
                targetType = row[2] != null ? m.classNameToRoot.get(row[2].toString()) : null;
            }
            if (targetType == null || (targetTypeFilter != null && targetType != targetTypeFilter)) {
                continue;
            }
            edges.add(new Edge(new EntityKey(descriptor.getReferrerType(), ((Number) row[0]).longValue()),
                    new EntityKey(targetType, targetId.longValue()), descriptor));
        }
    }

    private EntityKey readSoftReference(Model m, ReferenceDescriptor descriptor, Object entity) throws Exception {
        Object value = findField(entity.getClass(), descriptor.getField()).get(entity);
        if (!(value instanceof Number id)) {
            return null;
        }
        Class<?> targetType = descriptor.getTargetType();
        if (descriptor.getTargetClassField() != null) {
            Object className = findField(entity.getClass(), descriptor.getTargetClassField()).get(entity);
            targetType = className != null ? m.classNameToRoot.get(className.toString()) : null;
        }
        return targetType != null ? new EntityKey(targetType, id.longValue()) : null;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // purge
    // ---------------------------------------------------------------------------------------------------------------

    private void clearWeakReferences(Model m, Set<EntityKey> purge) throws Exception {
        Map<Class<?>, List<Long>> grouped = group(purge);
        for (ReferenceDescriptor descriptor : m.references) {
            if (descriptor.getPolicy() != Policy.WEAK) {
                continue;
            }
            Map<Long, Set<EntityKey>> targetsByReferrer = new LinkedHashMap<>();
            for (Map.Entry<Class<?>, List<Long>> group : grouped.entrySet()) {
                for (Edge edge : edgesByTarget(m, descriptor, group.getKey(), group.getValue())) {
                    if (!purge.contains(edge.referrer())) {
                        targetsByReferrer.computeIfAbsent(edge.referrer().id(), k -> new HashSet<>()).add(edge.target());
                    }
                }
            }
            if (targetsByReferrer.isEmpty()) {
                continue;
            }

            if (descriptor.isPersistent() && !descriptor.isCollection()) {
                String update = "UPDATE " + descriptor.getReferrerEntity() + " e SET e." + descriptor.getField() + " = NULL";
                if (descriptor.getTargetClassField() != null) {
                    update += ", e." + descriptor.getTargetClassField() + " = NULL";
                }
                for (List<Long> chunk : chunks(new ArrayList<>(targetsByReferrer.keySet()))) {
                    entityManager.createQuery(update + " WHERE e.id IN :ids").setParameter("ids", chunk).executeUpdate();
                }
                continue;
            }

            for (Map.Entry<Long, Set<EntityKey>> entry : targetsByReferrer.entrySet()) {
                Object referrer = entityManager.find(descriptor.getReferrerType(), entry.getKey());
                if (referrer == null) {
                    continue;
                }
                Field field = findField(referrer.getClass(), descriptor.getField());
                if (descriptor.isCollection()) {
                    Object value = field.get(referrer);
                    if (value instanceof Collection<?> collection) {
                        collection.removeIf(o -> o instanceof BaseEntity be
                                && entry.getValue().contains(new EntityKey(descriptor.getTargetType(), be.getId())));
                    }
                } else {
                    field.set(referrer, null);
                    if (descriptor.getTargetClassField() != null) {
                        findField(referrer.getClass(), descriptor.getTargetClassField()).set(referrer, null);
                    }
                    if (referrer instanceof ExtendableEntity extendable) {
                        extendableEntityListener.serialize(extendable);
                    }
                }
                if (referrer instanceof BaseEntity baseEntity) {
                    baseEntity.setModification(new Date());
                }
            }
            entityManager.flush();
        }
    }

    /**
     * Purged entities can reference each other (even in cycles, ie manuscript and its active message), hibernate
     * refuses to remove entity still referenced by loaded one, so references are nulled first.
     */
    private void detachReferences(Model m, Set<EntityKey> purge) {
        Map<Class<?>, List<Long>> grouped = group(purge);
        for (ReferenceDescriptor descriptor : m.references) {
            List<Long> ids = grouped.get(descriptor.getReferrerType());
            if (ids == null || !descriptor.isAssociation() || descriptor.isCollection()) {
                continue;
            }
            for (List<Long> chunk : chunks(ids)) {
                entityManager.createQuery("UPDATE " + descriptor.getReferrerEntity() + " e SET e." + descriptor.getField() + " = NULL WHERE e.id IN :ids")
                        .setParameter("ids", chunk)
                        .executeUpdate();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    private void remove(Set<EntityKey> purge) {
        for (Map.Entry<Class<?>, List<Long>> group : group(purge).entrySet()) {
            int count = 0;
            for (Long id : group.getValue()) {
                Object entity = entityManager.find(group.getKey(), id);
                if (entity != null) {
                    entityManager.remove(entity);
                }
                if (++count % REMOVE_BATCH_SIZE == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
            entityManager.flush();
            entityManager.clear();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------------------------------------------------

    private String label(EntityKey key) {
        try {
            Object entity = entityManager.find(key.type(), key.id());
            if (entity == null) {
                return key.toString();
            }
            for (String methodName : LABEL_METHODS) {
                try {
                    Method method = entity.getClass().getMethod(methodName);
                    Object value = method.invoke(entity);
                    if (value != null && StringUtils.isNotBlank(value.toString())) {
                        return value.toString();
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
            return entity instanceof BaseEntity baseEntity ? baseEntity.getUuid() : key.toString();
        } catch (Exception e) {
            log.debug("Failed to resolve label for {}", key, e);
            return key.toString();
        }
    }

    private Map<Class<?>, List<Long>> group(Collection<EntityKey> keys) {
        Map<Class<?>, List<Long>> grouped = new LinkedHashMap<>();
        for (EntityKey key : keys) {
            grouped.computeIfAbsent(key.type(), k -> new ArrayList<>()).add(key.id());
        }
        return grouped;
    }

    private List<List<Long>> chunks(List<Long> ids) {
        List<List<Long>> chunks = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += CHUNK_SIZE) {
            chunks.add(ids.subList(i, Math.min(ids.size(), i + CHUNK_SIZE)));
        }
        return chunks;
    }

    private Field findField(Class<?> cls, String name) throws NoSuchFieldException {
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(cls.getName() + "." + name);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
