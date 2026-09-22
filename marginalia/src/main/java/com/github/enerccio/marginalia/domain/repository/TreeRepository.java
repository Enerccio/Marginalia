package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.TreeEntity;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public interface TreeRepository<T extends TreeEntity> extends ExtendableRepository<T> {

    Long getPrevious(T entity, int levelSize, User user);
    Long getNext(T entity, int levelSize, User user);
    void changeTree(String oldTree, String newTree, User user);
    Long findByTree(String tree, User user);
    List<Long> getChildren(T entity, int levelSize, User user);
    int getChildrenCount(T entity, int levelSize, User user);
    List<String> getChildrenTrees(T entity, int levelSize, User user);
    Long getParent(T entity, int levelSize, User user);
    List<Long> getPath(T entity, int levelSize, User user) throws Exception;
    Long getRoot(int levelSize, User user);
    List<Long> getRoots(int levelSize, User user);
    List<String> getRootTrees(int levelSize, User user);
    String getMaxTree(int levelSize, User user) throws Exception;
    String getMaxTree(T entity, int levelSize, User user) throws Exception;
    Long getFirstChildId(String tree, int levelSize, User user) throws Exception;
    Long getLastChildId(String tree, int levelSize, User user) throws Exception;
    String getTreeForEntity(Long entityId, User user) throws Exception;
    List<Long> getNestedChildrenIds(String tree, Long selfId, User user) throws Exception;

}
