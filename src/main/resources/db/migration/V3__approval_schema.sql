-- 审批流主表
CREATE TABLE IF NOT EXISTS `approval_process` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `approval_no` varchar(50) NOT NULL COMMENT '审批单号',
  `approval_type` varchar(30) NOT NULL COMMENT '审批类型: TEST_RESULT检测结果, REJECT_HANDLE拒收处理, EXCEPTION异常处理',
  `biz_id` bigint NOT NULL COMMENT '业务单据ID',
  `biz_no` varchar(50) COMMENT '业务单据号',
  `title` varchar(200) NOT NULL COMMENT '审批标题',
  `apply_user_id` bigint NOT NULL COMMENT '申请人ID',
  `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `approval_status` varchar(30) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态: PENDING待审批, APPROVED已通过, REJECTED已驳回, CANCELLED已取消',
  `current_node` int NOT NULL DEFAULT 1 COMMENT '当前审批节点',
  `total_nodes` int NOT NULL COMMENT '总审批节点数',
  `final_approver_id` bigint COMMENT '最终审批人ID',
  `final_approve_time` datetime COMMENT '最终审批时间',
  `final_approve_remark` varchar(500) COMMENT '最终审批意见',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_approval_no` (`approval_no`),
  KEY `idx_biz` (`approval_type`, `biz_id`),
  KEY `idx_status` (`approval_status`),
  KEY `idx_apply_user` (`apply_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流主表';

-- 审批节点表
CREATE TABLE IF NOT EXISTS `approval_node` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_id` bigint NOT NULL COMMENT '审批流ID',
  `node_order` int NOT NULL COMMENT '节点序号',
  `node_name` varchar(50) NOT NULL COMMENT '节点名称',
  `approver_role` varchar(30) COMMENT '审批人角色',
  `approver_user_id` bigint COMMENT '指定审批人ID',
  `approve_user_id` bigint COMMENT '实际审批人ID',
  `approve_time` datetime COMMENT '审批时间',
  `approve_remark` varchar(500) COMMENT '审批意见',
  `approval_result` varchar(30) COMMENT '审批结果: APPROVED同意, REJECTED驳回',
  `node_status` varchar(30) NOT NULL DEFAULT 'PENDING' COMMENT '节点状态: PENDING待处理, PROCESSING处理中, COMPLETED已完成, SKIPPED已跳过',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_process` (`process_id`),
  KEY `idx_node_order` (`process_id`, `node_order`),
  KEY `idx_approver` (`approver_user_id`, `node_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批节点表';

-- 审批操作记录表
CREATE TABLE IF NOT EXISTS `approval_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_id` bigint NOT NULL COMMENT '审批流ID',
  `node_id` bigint COMMENT '审批节点ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operation_type` varchar(30) NOT NULL COMMENT '操作类型: SUBMIT提交, APPROVE同意, REJECT驳回, CANCEL取消, REASSIGN转办',
  `operation_remark` varchar(500) COMMENT '操作意见',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_process` (`process_id`),
  KEY `idx_operator` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批操作记录表';
