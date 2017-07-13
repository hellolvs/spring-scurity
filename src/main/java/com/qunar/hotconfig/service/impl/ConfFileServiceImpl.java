package com.qunar.hotconfig.service.impl;

import com.google.common.collect.Maps;
import com.qunar.hotconfig.dao.ConfFileDao;
import com.qunar.hotconfig.dao.ConfModifyRecordDao;
import com.qunar.hotconfig.model.ConfModifyRecordModel;
import com.qunar.hotconfig.service.ConfFileService;
import com.qunar.hotconfig.util.dateFormatUtil.MapDateFormatUtil;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by llnn.li on 2017/4/7.
 */
@Service
public class ConfFileServiceImpl implements ConfFileService {

    private final ConfFileDao confFileDao;
    private final ConfModifyRecordDao confModifyRecordDao;

    @Autowired
    public ConfFileServiceImpl(ConfFileDao confFileDao, ConfModifyRecordDao confModifyRecordDao) {
        this.confFileDao = confFileDao;
        this.confModifyRecordDao = confModifyRecordDao;
    }

    @Override
    public List<Map> selectByTableName(String fileId) {
        String[] fileIds = fileId.split("\\.");
        List<String> columnList = selectColumns(fileIds.length > 1 ? fileIds[1] : fileId);
        Map<String, Object> map = Maps.newHashMap();
        map.put("fileId", fileId);
        map.put("columnList", columnList);
        return confFileDao.selectByTableName(map);
    }

    @Override
    public Map selectByTableNameAndId(String fileId, Integer id) {
        String[] fileIds = fileId.split("\\.");
        List<String> columnList = selectColumns(fileIds.length > 1 ? fileIds[1] : fileId);
        Map<String, Object> map = Maps.newHashMap();
        map.put("fileId", fileId);
        map.put("id", id);
        map.put("columnList", columnList);
        return confFileDao.selectByTableNameAndId(map);
    }

    @Override
    public List<Map> selectByTableNameAsList(String fileId, RowBounds rowBounds) {
        List<Map> list = selectByTableName(fileId, rowBounds);
        return MapDateFormatUtil.formatDateTimeToString(list);
    }

    @Override
    public List<Map> selectByTableName(String fileId, RowBounds rowBounds) {
        String[] fileIds = fileId.split("\\.");
        List<String> columnList = selectColumns(fileIds.length > 1 ? fileIds[1] : fileId);
        Map<String, Object> map = Maps.newHashMap();
        map.put("fileId", fileId);
        map.put("columnList", columnList);
        return confFileDao.selectByTableName(map, rowBounds);
    }

    @Override
    public List<String> selectColumns(String fileId) {
        return confFileDao.selectColumns(fileId);
    }

    @Override
    public Integer count(String fileId) {
        Map<String, String> map = Maps.newHashMap();
        map.put("fileId", fileId);
        return confFileDao.count(map);
    }

    @Transactional
    @Override
    public int insertByTableName(String fileId, Map columnValuesMap) {
        Map<String, Object> map = Maps.newHashMap();
        String[] insertSqlValues = joinInsertSqlValues(fileId, columnValuesMap);
        map.put("id", null);
        map.put("fileId", fileId);
        map.put("columns", insertSqlValues[0]);
        map.put("columnsValues", insertSqlValues[1]);
        int result = confFileDao.insertByTableName(map);
        // 写入配置修改记录表
        writeToConfModifyRecord(fileId, (Integer) map.get("id"), 0);
        return result;
    }

    @Override
    public int batchInsertByTableName(String fileId, List<Map> mapList) {
        Map<String, Object> map = Maps.newHashMap();
        String[] batchInsertSqlValues = joinBatchInsertSqlValues(fileId, mapList);

        map.put("fileId", fileId);
        map.put("columns", batchInsertSqlValues[0]);
        map.put("columnsValues", batchInsertSqlValues[1]);
        return confFileDao.batchInsertByTableName(map);
    }

    @Transactional
    @Override
    public int updateByPrimaryKey(String fileId, Map columnValuesMap) {
        Map<String, Object> map = Maps.newHashMap();
        String updateSqlValues = joinUpdateSqlValues(fileId, columnValuesMap);

        Integer id = (Integer) columnValuesMap.get("id");
        map.put("fileId", fileId);
        map.put("id", id);
        map.put("updateValues", updateSqlValues);
        int num = confFileDao.updateByPrimaryKey(map);
        // 写入配置修改记录表
        writeToConfModifyRecord(fileId, id, 2);
        return num;
    }

    @Transactional
    @Override
    public int deleteById(String fileId, Integer id) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("fileId", fileId);
        map.put("id", id);
        int num = confFileDao.deleteById(map);
        // 写入配置修改记录表
        writeToConfModifyRecord(fileId, id, 1);
        return num;
    }

    @Override
    public int deleteTable(String fileId) {
        return confFileDao.deleteTable(fileId);
    }

    /**
     * 拼装插入sql语句 INSERT INTO ${fileId} (${columns}) VALUES ${columnsValues};
     *
     * @param fileId 表名
     * @param columnValuesMap 参数值
     * @return 插入sql的变量 insertSqlValues[0]==${columns} insertSqlValues[1]==(${columnsValues})
     */
    private String[] joinInsertSqlValues(String fileId, Map columnValuesMap) {

        String[] insertSqlValues = new String[2];
        String[] fileIds = fileId.split("\\.");
        if (fileIds.length > 1) {
            fileId = fileIds[1];
        }
        List<String> columnList = selectColumns(fileId);

        StringBuilder columnsBuilder = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        for (String key : columnList) {
            if (key.equals("id")) {
                continue;
            }
            if (columnValuesMap.containsKey(key)) {
                Object object = columnValuesMap.get(key);
                if (object instanceof String || object instanceof Date || object instanceof Time
                        || object instanceof Timestamp) {
                    builder.append("\'");
                    builder.append(columnValuesMap.get(key));
                    builder.append("\'");
                } else {
                    builder.append(columnValuesMap.get(key));
                }
                builder.append(",");
                columnsBuilder.append(key);
                columnsBuilder.append(",");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        columnsBuilder.deleteCharAt(columnsBuilder.length() - 1);

        insertSqlValues[0] = columnsBuilder.toString();
        insertSqlValues[1] = builder.toString();
        return insertSqlValues;
    }

    /**
     * 拼装批量插入sql语句 INSERT INTO ${fileId} (${columns}) VALUES ${columnsValues};
     *
     * @param fileId 表名
     * @param mapList 批量插入的多行数据
     * @return 插入sql的变量 batchInsertSqlValues[0]==${columns} batchInsertSqlValues[1]==(${columnsValues})
     */
    private String[] joinBatchInsertSqlValues(String fileId, List<Map> mapList) {

        String[] batchInsertSqlValues = new String[2];
        String[] fileIds = fileId.split("\\.");
        if (fileIds.length > 1) {
            fileId = fileIds[1];
        }
        List<String> columnList = selectColumns(fileId);

        StringBuilder columnsBuilder = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        for (String key : columnList) {
            columnsBuilder.append(key);
            columnsBuilder.append(",");
        }
        for (Map map : mapList) {
            builder.append("(");
            for (String key : columnList) {
                Object object = map.get(key);
                if (object instanceof String || object instanceof Date || object instanceof Time
                        || object instanceof Timestamp) {
                    builder.append("\'");
                    builder.append(map.get(key));
                    builder.append("\'");
                } else {
                    builder.append(map.get(key));
                }
                builder.append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append("),");
        }
        builder.deleteCharAt(builder.length() - 1);
        columnsBuilder.deleteCharAt(columnsBuilder.length() - 1);

        batchInsertSqlValues[0] = columnsBuilder.toString();
        batchInsertSqlValues[1] = builder.toString();
        return batchInsertSqlValues;
    }

    /**
     * 拼装更新sql语句 update ${fileId} set ${updateValues} where id = #{id}
     *
     * @param fileId 表名
     * @param columnValuesMap 参数值
     * @return 更新sql的变量updateSqlValues==${updateValues}
     */
    private String joinUpdateSqlValues(String fileId, Map columnValuesMap) {
        String[] fileIds = fileId.split("\\.");
        if (fileIds.length > 1) {
            fileId = fileIds[1];
        }
        List<String> columnList = selectColumns(fileId);

        StringBuilder builder = new StringBuilder();
        for (String key : columnList) {
            if (key.equals("id")) {
                continue;
            }
            if (columnValuesMap.containsKey(key)) {
                builder.append(key).append("=");
                Object object = columnValuesMap.get(key);
                if (object instanceof String || object instanceof Date || object instanceof Time
                        || object instanceof Timestamp) {
                    builder.append("\'");
                    builder.append(columnValuesMap.get(key));
                    builder.append("\'");
                } else {
                    builder.append(columnValuesMap.get(key));
                }
                builder.append(",");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    @Override
    public List<ConfModifyRecordModel> selectModifyRecordByFileId(String fileId) {
        return confModifyRecordDao.selectModifyRecordByFileId(fileId);
    }

    @Override
    public ConfModifyRecordModel selectModifyRecord(String fileId, Integer itemId) {
        return confModifyRecordDao.selectModifyRecord(fileId, itemId);
    }

    @Override
    public int insertConfModifyRecord(ConfModifyRecordModel confModifyRecordModel) {
        return confModifyRecordDao.insertConfModifyRecord(confModifyRecordModel);
    }

    @Override
    public int deleteModifyRecordByFileId(String fileId) {
        return confModifyRecordDao.deleteModifyRecordByFileId(fileId);
    }

    @Override
    public int deleteModifyRecordById(Integer id) {
        return confModifyRecordDao.deleteModifyRecordById(id);
    }

    @Override
    public int updateModifyRecord(ConfModifyRecordModel confModifyRecordModel) {
        return confModifyRecordDao.updateModifyRecord(confModifyRecordModel);
    }

    /**
     * 写入配置修改记录表
     *
     * @param fileId
     * @param id
     * @param crudType
     */
    private void writeToConfModifyRecord(String fileId, Integer id, Integer crudType) {
        if (crudType == 0) {// 当是添加配置项时
            insertConfModifyRecord(new ConfModifyRecordModel(fileId, id, Short.valueOf("0")));
        } else if (crudType == 1) {// 当是删除配置项时
            ConfModifyRecordModel confModifyRecordModel = selectModifyRecord(fileId, id);
            if (confModifyRecordModel != null) {
                if (confModifyRecordModel.getCrudType() == 2) {// 只有当之前是修改该记录的时候才更改操作类型，如果是增加，则删除操作相对于线上库没有改变
                    confModifyRecordModel.setCrudType(Short.valueOf("1"));// 删除
                    updateModifyRecord(confModifyRecordModel);
                } else if (confModifyRecordModel.getCrudType() == 0) {// 如果这条记录之前是增加的，那么现在这条记录删除了，在配置更改记录表中应该删除
                    deleteModifyRecordById(confModifyRecordModel.getId());
                }
            } else {
                insertConfModifyRecord(new ConfModifyRecordModel(fileId, id, Short.valueOf("1")));
            }
        } else if (crudType == 2) {// 当是修改配置项时
            ConfModifyRecordModel confModifyRecordModel = selectModifyRecord(fileId, id);
            if (confModifyRecordModel == null) {
                // 校验是否真的有修改
                Map map1 = selectByTableNameAndId("conffilesdb." + fileId, id);
                Map map2 = selectByTableNameAndId("conffilesmanage." + fileId, id);
                map1 = MapDateFormatUtil.formatDateTimeToString(map1);
                map2 = MapDateFormatUtil.formatDateTimeToString(map2);
                int flag = isModified(map1, map2);
                if (flag == 1) {// 当该记录是首次修改且和conffilesdb库内容不同时，标记修改
                    confModifyRecordModel = new ConfModifyRecordModel(fileId, id, Short.valueOf("2"));
                    insertConfModifyRecord(confModifyRecordModel);
                }
            } else if (confModifyRecordModel.getCrudType() == 2) {// 如果之前修改过
                // 校验是否真的有修改
                Map map1 = selectByTableNameAndId("conffilesdb." + fileId, id);
                Map map2 = selectByTableNameAndId("conffilesmanage." + fileId, id);
                map1 = MapDateFormatUtil.formatDateTimeToString(map1);
                map2 = MapDateFormatUtil.formatDateTimeToString(map2);
                int flag = isModified(map1, map2);
                if (flag == 0) {// conffilesdb和conffilesmanage没有区别
                    deleteModifyRecordById(confModifyRecordModel.getId());
                }
            }
        }
    }

    // 判断map1和map2是否有区别
    private int isModified(Map map1, Map map2) {
        Set<String> columnKeySet = map1.keySet();
        int flag = 0;
        for (String columnKey : columnKeySet) {
            if (!map1.get(columnKey).equals(map2.get(columnKey))) {
                flag = 1;// 确实有修改
            }
        }
        return flag;
    }

}
