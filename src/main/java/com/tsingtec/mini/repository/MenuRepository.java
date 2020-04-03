package com.tsingtec.mini.repository;

import com.tsingtec.mini.entity.sys.Menu;
import com.tsingtec.mini.entity.sys.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Integer>, JpaSpecificationExecutor<Menu> {

    @Modifying
    @Transactional
    @Query("delete from Menu m where m.id in (?1)")
    void deleteBatch(List<Integer> ids);

    List<Menu> findByPid(Integer pid);

    /**
     * 取消角色与菜单之间的关系
     * @param ids 角色ID
     * @return 影响结果
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM sys_role_menu WHERE mid = ?1", nativeQuery = true)
    void cancelMenuJoin(Integer ids);
}