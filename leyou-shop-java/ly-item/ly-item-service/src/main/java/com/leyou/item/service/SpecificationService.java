package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;

import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper groupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {
        // 查询条件
        SpecGroup group = new SpecGroup();
        group.setCid(cid);
        List<SpecGroup> list = groupMapper.select(group);
        if(CollectionUtils.isEmpty(list)){
            // 没查到 抛出异常
            throw new LyException(ExceptionEnums.SPEC_GROUP_NOT_FOUND);
        }
        return list;
    }

//    public List<SpecParam> queryParamByGid(Long gid) {
//        SpecParam param = new SpecParam();
//        param.setGroupId(gid);
//        List<SpecParam> list = specParamMapper.select(param);
//        if(CollectionUtils.isEmpty(list)){
//            // 没查到 抛出异常
//            throw new LyException(ExceptionEnums.SPEC_PARAM_NOT_FOUND);
//        }
//        return list;
//    }

    /**
     *
     * @param gid 组id
     * @param cid 分类id
     * @param searching 是否搜索
     * @return
     */
    public List<SpecParam> queryParamList(Long gid, Long cid, Boolean searching) {
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);
        List<SpecParam> list = specParamMapper.select(param);
        if(CollectionUtils.isEmpty(list)){
            // 没查到 抛出异常
            throw new LyException(ExceptionEnums.SPEC_PARAM_NOT_FOUND);
        }
        return list;
    }

    public List<SpecGroup> queryListByCid(Long cid) {
        // 查询规格组
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        // 查询分类下的参数
        List<SpecParam> specParams = queryParamList(null, cid, null);
        // 先把规格参数变为map key 为 规格组id  值是组下的所有参数
        Map<Long,List<SpecParam>> params = new HashMap<>();
        for (SpecParam param : specParams) {
            if(!params.containsKey(param.getGroupId())){
                // 组id在map中不存在
                params.put(param.getGroupId(),new ArrayList<>());
            }
            params.get(param.getGroupId()).add(param);
        }
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(params.get(specGroup.getId()));
        }
        System.out.println("specGroups = " + specGroups);
        return specGroups;

    }
}
