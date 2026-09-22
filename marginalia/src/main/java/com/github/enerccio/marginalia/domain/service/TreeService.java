package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.TreeEntity;
import com.github.enerccio.marginalia.domain.repository.TreeRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;

import java.util.List;

public interface TreeService<T extends TreeEntity, R extends TreeRepository<T>> extends ExtendableService<T, R> {

    T addChild(T entity, T child) throws Exception;
    T moveChild(T entity, T child) throws Exception;
    boolean canAddSibling(T entity, boolean pre) throws Exception;
    T addSibling(T sibling, T newSibling, boolean pre) throws Exception;
    T moveChild(T sibling, T newSibling, boolean pre) throws Exception;
    T getPrevious(T entity) throws Exception;
    T getNext(T entity) throws Exception;
    T changeTree(T entity, String newTree) throws Exception;
    T findByTree(String tree) throws Exception;
    List<T> getChildren(T entity) throws Exception;
    List<String> getChildrenTrees(T entity) throws Exception;
    List<Long> getChildrenIds(T entity) throws Exception;
    boolean hasChildren(T entity) throws Exception;
    int getChildrenCount(T entity) throws Exception;
    T getFirstChild(T entity) throws Exception;
    Long getFirstChildId(T entity) throws Exception;
    T getLastChild(T entity) throws Exception;
    Long getLastChildId(T entity) throws Exception;
    T getParent(T entity) throws Exception;

    @CommonTxReadOnly
    List<T> getPath(T entity) throws Exception;

    @CommonTxReadOnly
    T getRoot() throws Exception;

    @CommonTxReadOnly
    List<T> getRoots() throws Exception;

    @CommonTxReadOnly
    List<String> getRootTrees() throws Exception;

    @CommonTxReadOnly
    List<String> getRootTrees(User user) throws Exception;

    @CommonTxReadOnly
    List<Long> getRootIds() throws Exception;

    long tree2num(String tree);
    int getLevelSize();
    String updateTree(String tree, Integer newOrdinal);
    long levelMax();
    String getTreeForEntity(Long entityId) throws Exception;
    List<Long> getNestedChildrenIds(T entity) throws Exception;
    String getMaxRootTree() throws Exception;
    String incTree(String tree, long value);
    String getMaxTree(T entity) throws Exception;

    @CommonTxReadOnly
    String getMaxTree(T entity, User user) throws Exception;

    boolean isMoveAvailable(T movee, T moveUnder);

}
