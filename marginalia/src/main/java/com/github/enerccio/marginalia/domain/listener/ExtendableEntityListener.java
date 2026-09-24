package com.github.enerccio.marginalia.domain.listener;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.github.enerccio.marginalia.utils.ReflectUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.persistence.PostLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtendableEntityListener {
    private static final Logger log = LoggerFactory.getLogger(ExtendableEntityListener.class);

    private static final ConcurrentHashMap<Class<?>, AttributesAccessor> accessorMap = new ConcurrentHashMap<>();

    public void serialize(ExtendableEntity entity) throws Exception {
        accessorMap.computeIfAbsent(entity.getClass(), key -> {
            try {
                return new AttributesAccessor(entity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).serialize(entity);
    }

    @PostLoad
    public void deserialize(ExtendableEntity entity) throws Exception {
        accessorMap.computeIfAbsent(entity.getClass(), key -> {
            try {
                return new AttributesAccessor(entity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).deserialize(entity);
    }

    private static class AttributesAccessor {
        private final Gson gson = new GsonBuilder().serializeNulls().create();
        private final ThreadLocal<SimpleDateFormat> df = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy.MM.dd'Z'HH:mm:ss.SSS"));

        private final Class<?> clazz;
        private final Map<String, Field> attributes = new HashMap<>();

        public AttributesAccessor(Object handle) throws Exception {
            clazz = handle.getClass();
            combClass();
        }

        private void combClass() throws Exception {
            List<Field> fields = ReflectUtils.getAnnotatedFields(clazz, ExtendedAttribute.class);
            for (Field f : fields) {
                attributes.put(f.getName(), f);
            }
        }

        public void serialize(ExtendableEntity entity) throws Exception {
            JsonObject root = new JsonObject();
            for (String field : attributes.keySet()) {
                Field f = attributes.get(field);
                ExtendedAttribute extAttrAnnot = f.getAnnotation(ExtendedAttribute.class);

                if (!extAttrAnnot.inject()) {
                    Object value = f.get(entity);
                    String strValue = value == null ? null : value.toString();
                    if (strValue == null)
                        continue;

                    if (value instanceof Date)
                        strValue = df.get().format((Date) value);

                    root.addProperty(field, strValue);
                } else {
                    JsonObject injected = (JsonObject) f.get(entity);
                    if (injected == null) {
                        continue;
                    }

                    for (String key : injected.keySet()) {
                        root.add(extAttrAnnot.injectPrefix() + "_" + key, injected.get(key));
                    }
                }
            }

            entity.setExtendedContent(gson.toJson(root).getBytes(StandardCharsets.UTF_8));
        }

        public void deserialize(ExtendableEntity entity) throws Exception {
            if (entity == null || entity.getExtendedContent() == null || entity.getExtendedContent().length < 2)
                return;

            JsonObject root = gson.fromJson(new String(entity.getExtendedContent(), StandardCharsets.UTF_8), JsonObject.class);
            Map<String, JsonObject> injectedObjects = new HashMap<>();

            for (String field : attributes.keySet()) {
                Field f = attributes.get(field);
                ExtendedAttribute extAttrAnnot = f.getAnnotation(ExtendedAttribute.class);
                if (extAttrAnnot.inject()) {
                    injectedObjects.put(field, new JsonObject());
                    continue; // second pass
                }

                JsonElement element = root.get(field);
                processElement(f, element, entity);
            }

            for (String field : injectedObjects.keySet()) {
                JsonObject object = injectedObjects.get(field);
                Field f = attributes.get(field);
                ExtendedAttribute extAttrAnnot = f.getAnnotation(ExtendedAttribute.class);
                String prefix = extAttrAnnot.injectPrefix() + "_";
                for (String key : root.keySet()) {
                    if (key.startsWith(prefix)) {
                        JsonElement element = root.get(key);
                        object.add(key.substring(prefix.length()), element);
                    }
                }
                f.set(entity, object);
            }
        }

        private void processElement(Field f, JsonElement element, ExtendableEntity entity) throws Exception {
            if (element != null) {
                String strVal = element.getAsString();

                try {
                    if (strVal == null || strVal.trim().isEmpty()) {
                        if (f.getType() == boolean.class) {
                            f.set(entity, false);
                        } else if (String.class.equals(f.getType())) {
                            f.set(entity, strVal != null ? strVal : "");
                        }
                        return;
                    }

                    Class<?> type = f.getType();

                    if (String.class.equals(type)) {
                        f.set(entity, strVal);
                    } else if (Integer.class.equals(type) || int.class.equals(type)) {
                        f.set(entity, Integer.valueOf(strVal));
                    } else if (Long.class.equals(type) || long.class.equals(type)) {
                        f.set(entity, Long.valueOf(strVal));
                    } else if (Double.class.equals(type) || double.class.equals(type)) {
                        f.set(entity, Double.valueOf(strVal));
                    } else if (Float.class.equals(type) || float.class.equals(type)) {
                        f.set(entity, Float.valueOf(strVal));
                    } else if (Date.class.equals(type)) {
                        f.set(entity, df.get().parse(strVal));
                    } else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
                        f.set(entity, Boolean.valueOf(strVal));
                    }
                } catch (Exception e) {
                    log.error("Error deserialize entity ({}), id: {}, string value: {}, error {}({})", entity.getClass().getSimpleName(), entity.getId(), strVal, e.getClass(), e.getMessage());
                }
            }
        }
    }

}
