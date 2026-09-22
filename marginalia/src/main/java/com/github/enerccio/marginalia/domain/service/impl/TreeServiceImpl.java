package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Constants;
import com.github.enerccio.marginalia.domain.model.TreeEntity;
import com.github.enerccio.marginalia.domain.repository.TreeRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.TreeService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.NoTx;

import java.util.ArrayList;
import java.util.List;

public class TreeServiceImpl<T extends TreeEntity, R extends TreeRepository<T>> extends ExtendableServiceImpl<T, R> implements TreeService<T, R> {

    private static final char[] ciphers = {
            'A', 'B', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private StringBuffer zero;
    private long levelMax = 0;
    private int levelSize = Constants.TREE_DEFAULT_LEVEL_SIZE;

    @Override
    @CommonTx
    public T addChild(T entity, T child) throws Exception {
        List<String> children = getChildrenTrees(entity);

        String childTree = entity.getTree();

        if (children.isEmpty()) {
            childTree += Constants.TREE_DEFAULT_LEVEL;
        } else {
            childTree += incTree(children.getLast().substring(childTree.length()), Constants.TREE_DEFAULT_ALLOC_GAP);
        }

        child.setTree(childTree);
        return find(save(child));
    }

    @Override
    @CommonTx
    public T moveChild(T entity, T child) throws Exception {
        List<String> children = getChildrenTrees(entity);

        String childTree = entity.getTree();

        if (children.isEmpty()) {
            childTree += Constants.TREE_DEFAULT_LEVEL;
        } else {
            childTree += incTree(children.getLast().substring(childTree.length()), Constants.TREE_DEFAULT_ALLOC_GAP);
        }

        return find(changeTree(child, childTree));
    }

    @Override
    @CommonTxReadOnly
    public boolean canAddSibling(T entity, boolean pre) throws Exception {
        return generateNextOrdinalForInsert(entity, entity.getOwner(), pre) != null;
    }

    @Override
    @CommonTx
    public T addSibling(T sibling, T newSibling, boolean pre) throws Exception {
        String tree = generateNextOrdinalForInsert(sibling, sibling.getOwner(), pre);
        if (tree == null)
            throw new IllegalStateException("Cannot insert sibling");
        newSibling.setTree(tree);
        return find(save(newSibling));
    }

    @Override
    @CommonTx
    public T moveChild(T sibling, T newSibling, boolean pre) throws Exception {
        String tree = generateNextOrdinalForInsert(sibling, sibling.getOwner(), pre);
        if (tree == null)
            throw new IllegalStateException("Cannot insert sibling");

        return find(changeTree(newSibling, tree));
    }

    @Override
    @CommonTx
    public T getPrevious(T entity) throws Exception {
        return find(getRepository().getPrevious(entity, getLevelSize(), entity.getOwner()));
    }

    @Override
    @CommonTxReadOnly
    public T getNext(T entity) throws Exception {
        return find(getRepository().getNext(entity, getLevelSize(), entity.getOwner()));
    }

    @Override
    @CommonTx
    public T changeTree(T entity, String newTree) throws Exception {
        String oldTree = entity.getTree();

        if (oldTree.equals(newTree))
            return entity;

        getRepository().changeTree(oldTree, newTree, entity.getOwner());

        entity.setTree(newTree);
        entity = save(entity);

        return entity;
    }

    @Override
    @CommonTxReadOnly
    public T findByTree(String tree) throws Exception {
        return find(getRepository().findByTree(tree, currentUser));
    }

    @Override
    @CommonTxReadOnly
    public List<T> getChildren(T entity) throws Exception {
        List<T> children = new ArrayList<>();

        for (Long id : getRepository().getChildren(entity, getLevelSize(), entity.getOwner())) {
            children.add(find(id));
        }

        return children;
    }

    @Override
    @CommonTxReadOnly
    public List<String> getChildrenTrees(T entity) throws Exception {
        return getRepository().getChildrenTrees(entity, getLevelSize(), entity.getOwner());
    }

    @Override
    @CommonTxReadOnly
    public List<Long> getChildrenIds(T entity) throws Exception {
        return getRepository().getChildren(entity, getLevelSize(), entity.getOwner());
    }

    @Override
    @CommonTxReadOnly
    public boolean hasChildren(T entity) throws Exception {
        return getRepository().getChildrenCount(entity, getLevelSize(), entity.getOwner()) > 0;
    }

    @Override
    @CommonTxReadOnly
    public int getChildrenCount(T entity) throws Exception {
        return getRepository().getChildrenCount(entity, getLevelSize(), entity.getOwner());
    }

    @Override
    @CommonTxReadOnly
    public T getFirstChild(T entity) throws Exception {
        Long id = getFirstChildId(entity);
        return id == null ? null : find(id);
    }

    @Override
    @CommonTxReadOnly
    public Long getFirstChildId(T entity) throws Exception {
        return getRepository().getFirstChildId(entity.getTree(), getLevelSize(), entity.getOwner());
    }

    @Override
    @CommonTxReadOnly
    public T getLastChild(T entity) throws Exception {
        Long id = getLastChildId(entity);
        return id == null ? null : find(id);
    }

    @Override
    @CommonTxReadOnly
    public Long getLastChildId(T entity) throws Exception {
        return getRepository().getLastChildId(entity.getTree(), getLevelSize(), entity.getOwner());
    }

    @Override
    @CommonTxReadOnly
    public T getParent(T entity) throws Exception {
        return find(getRepository().getParent(entity, getLevelSize(), entity.getOwner()));
    }

    @CommonTxReadOnly
    @Override
    public List<T> getPath(T entity) throws Exception {
        List<T> path = new ArrayList<>();

        for (Long id : getRepository().getPath(entity, getLevelSize(), entity.getOwner())) {
            path.add(find(id));
        }

        return path;
    }

    @CommonTxReadOnly
    @Override
    public T getRoot() throws Exception {
        return find(getRepository().getRoot(getLevelSize(), currentUser));
    }

    @CommonTxReadOnly
    @Override
    public List<T> getRoots() throws Exception {
        List<T> roots = new ArrayList<>();
        for (Long id : getRepository().getRoots(getLevelSize(), currentUser))
            roots.add(find(id));

        return roots;
    }

    @CommonTxReadOnly
    @Override
    public List<String> getRootTrees() throws Exception {
        return getRepository().getRootTrees(getLevelSize(), currentUser);
    }

    @CommonTxReadOnly
    @Override
    public List<String> getRootTrees(User user) throws Exception {
        return getRepository().getRootTrees(getLevelSize(), user);
    }

    @CommonTxReadOnly
    @Override
    public List<Long> getRootIds() throws Exception {
        return getRepository().getRoots(getLevelSize(), currentUser);
    }

    @Override
    @CommonTxReadOnly
    public long tree2num(String tree) {
        long n = 0;
        StringBuilder b = new StringBuilder(tree);

        int  i  = getLevelSize() - 1;
        long m  = getCipherCount();
        int  ii = 0;

        for (; i >= 0; i--, ii++) {
            n += getCipherValue(b.charAt(i)) * (long)Math.pow(m, ii);
        }

        return n;
    }

    @Override
    public int getLevelSize() {
        return levelSize;
    }

    @Override
    @CommonTx
    public String updateTree(String tree, Integer newOrdinal) {
        int offset = tree.length() - getLevelSize();
        String left	= tree.substring(0, offset);
        return left + num2tree(newOrdinal);
    }

    @Override
    @CommonTxReadOnly
    public long levelMax() {
        if (levelMax == 0)
            levelMax = (long) Math.pow(getCipherCount(), getLevelSize()) - 1;

        return levelMax;
    }

    @Override
    @CommonTxReadOnly
    public String getTreeForEntity(Long entityId) throws Exception {
        if (entityId == null)
            return null;
        return getRepository().getTreeForEntity(entityId, currentUser);
    }

    @Override
    @CommonTxReadOnly
    public List<Long> getNestedChildrenIds(T entity) throws Exception {
        return getRepository().getNestedChildrenIds(entity.getTree(), entity.getId(), entity.getOwner());
    }

    @Override
    @CommonTxReadOnly
    public String getMaxRootTree() throws Exception {
        String maxTree = getRepository().getMaxTree(getLevelSize(), currentUser);
        if (maxTree == null)
            maxTree = num2tree(0L);
        return maxTree;
    }

    @Override
    @CommonTxReadOnly
    public String incTree(String tree, long by) {
        int offset = tree.length() - getLevelSize();
        String left	= tree.substring(0, offset);
        String lastLevel = tree.substring(offset);

        long n = tree2num(lastLevel);
        n += by;
        if (n < 0 || n > levelMax()) {
            throw new IllegalArgumentException(getClass().getName() +
                    ".inc()" + ": Result out of range.");
        }
        lastLevel = num2tree(n);

        return left + lastLevel;
    }

    @Override
    @CommonTxReadOnly
    public String getMaxTree(T entity) throws Exception {
        String maxTree = getRepository().getMaxTree(entity, getLevelSize(), entity.getOwner());
        if (maxTree == null)
            maxTree = entity.getTree() + num2tree(0L);
        return maxTree;
    }

    @CommonTxReadOnly
    @Override
    public String getMaxTree(T entity, User user) throws Exception {
        String maxTree = getRepository().getMaxTree(entity, getLevelSize(), user);
        if (maxTree == null)
            maxTree = entity.getTree() + num2tree(0L);
        return maxTree;
    }

    @Override
    @NoTx
    public boolean isMoveAvailable(T movee, T moveUnder) {
        String tree = moveUnder.getTree();
        String mtree = movee.getTree();
        if (tree.length() > mtree.length()) {
            tree = tree.substring(0, mtree.length());
        }
        return !tree.equals(mtree);
    }

    @NoTx
    protected String generateNextOrdinalForInsert(T entity, User user, boolean pre) throws Exception {
        T parent = getParent(entity);

        List<String> children;
        int parentLength;
        String parentTree;
        if (parent == null) {
            children = getRootTrees(user);
            parentLength = 0;
            parentTree = "";
        } else {
            children = getChildrenTrees(parent);
            parentLength = parent.getTree().length();
            parentTree = parent.getTree();
        }

        String siblingOrdinal = null;
        String nextOrdinal = null;
        siblingOrdinal = entity.getTree().substring(parentLength);
        for (int i=0; i<children.size(); i++) {
            String sibling = children.get(i);
            if (entity.getTree().equals(sibling)) {
                if (pre) {
                    if (i > 0) {
                        nextOrdinal = children.get(i - 1).substring(parentLength);
                    }
                } else {
                    if (i != children.size() - 1) {
                        nextOrdinal = children.get(i + 1).substring(parentLength);
                    }
                }
                break;
            }
        }

        long ordinalNum = tree2num(siblingOrdinal);
        Long ordinalNewNum = null;
        if (pre) {
            if (nextOrdinal != null) {
                // is between ordinals
                long ordinalPrev = tree2num(nextOrdinal);
                if (ordinalNum - ordinalPrev > 1) {
                    ordinalNewNum = ordinalPrev + (long) Math.floor((ordinalNum - ordinalPrev) / 2.0);
                }
                // if ordinalNewNum is null we ran out of space, fail
            } else if (ordinalNum != 0) {
                // will be next first ordinal
                if (ordinalNum > Constants.TREE_DEFAULT_ALLOC_GAP) {
                    // reset position to Constants.TREE_DEFAULT_ALLOC_GAP step
                    ordinalNewNum = ordinalNum - (ordinalNum % Constants.TREE_DEFAULT_ALLOC_GAP) -
                            Constants.TREE_DEFAULT_ALLOC_GAP;
                } else {
                    // too little space left, just half the difference
                    ordinalNewNum = (long) Math.floor((ordinalNum * 1.0) / 2.0);
                }
                // if ordinalNewNum is null we ran out of space, fail
            }
        } else {
            if (nextOrdinal != null) {
                // is between ordinals
                long ordinalNext = tree2num(nextOrdinal);
                if (ordinalNext - ordinalNum > 1) {
                    ordinalNewNum = ordinalNum + (long) Math.floor((ordinalNext - ordinalNum) / 2.0);
                }
                // if ordinalNewNum is null we ran out of space, fail
            } else {
                // will be next last ordinal
                if (ordinalNum != levelMax()) {
                    // reset position to Constants.TREE_DEFAULT_ALLOC_GAP step
                    ordinalNewNum = ordinalNum + (Constants.TREE_DEFAULT_ALLOC_GAP - (ordinalNum % Constants.TREE_DEFAULT_ALLOC_GAP))
                            + Constants.TREE_DEFAULT_ALLOC_GAP;
                }
                // if ordinalNewNum is null we ran out of space, fail
            }
        }

        if (ordinalNewNum == null)
            return null;
        return parentTree + num2tree(ordinalNewNum);
    }

    @NoTx
    protected String num2tree(long n) {
        StringBuilder tree = new StringBuilder(zero().toString());

        long cipherCount = getCipherCount();

        int m;
        int o = 1;
        int i = getLevelSize() - 1;

        for (; i >= 0; i--) {
            if (n == 0) break;

            m  = (int)(n % cipherCount);
            n -= m;
            n /= cipherCount * o;
            tree.setCharAt(i, ciphers[m]);
        }

        return tree.toString();
    }

    private int getCipherCount() {
        return ciphers.length;
    }

    private int getCipherValue(char cipher) {
        int cipherCount = getCipherCount();

        for (int i = 0; i < cipherCount; i++) {
            if (ciphers[i] == cipher) {
                return i;
            }
        }

        return -1;
    }

    private StringBuffer zero() {
        if (zero == null) {
            char _zeroCipher = ciphers[0];
            zero = new StringBuffer(getLevelSize());

            for (int i = 0; i < getLevelSize(); i++) {
                zero.append(_zeroCipher);
            }
        }

        return zero;
    }

    public void setLevelSize(int levelSize) {
        this.levelSize = levelSize;
    }
}
