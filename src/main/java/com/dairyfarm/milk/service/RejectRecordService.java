package com.dairyfarm.milk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.context.UserContext;
import com.dairyfarm.milk.common.exception.BusinessException;
import com.dairyfarm.milk.entity.RejectRecord;
import com.dairyfarm.milk.mapper.RejectRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RejectRecordService {

    private final RejectRecordMapper rejectRecordMapper;

    public Page<RejectRecord> getRejectList(Long pageNum, Long pageSize, String handleStatus, String rejectType) {
        Long pastureId = UserContext.getPastureId();
        LambdaQueryWrapper<RejectRecord> wrapper = new LambdaQueryWrapper<>();
        if (pastureId != null) {
            wrapper.eq(RejectRecord::getPastureId, pastureId);
        }
        if (handleStatus != null && !handleStatus.isEmpty()) {
            wrapper.eq(RejectRecord::getHandleStatus, handleStatus);
        }
        if (rejectType != null && !rejectType.isEmpty()) {
            wrapper.eq(RejectRecord::getRejectType, rejectType);
        }
        wrapper.orderByDesc(RejectRecord::getCreateTime);
        return rejectRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public RejectRecord getRejectDetail(Long id) {
        return rejectRecordMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public RejectRecord handleReject(Long id, String handleStatus, String handleResult) {
        RejectRecord reject = rejectRecordMapper.selectById(id);
        if (reject == null) {
            throw new BusinessException("拒收记录不存在");
        }

        reject.setHandlerId(UserContext.getUserId());
        reject.setHandleStatus(handleStatus);
        reject.setHandleResult(handleResult);
        if ("RESOLVED".equals(handleStatus)) {
            reject.setHandleTime(LocalDateTime.now());
        }
        rejectRecordMapper.updateById(reject);

        log.info("处理拒收记录，拒收单号: {}, 处理状态: {}", reject.getRejectNo(), handleStatus);
        return reject;
    }
}
