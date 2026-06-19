-- 抗生素检测表增加复检相关字段
ALTER TABLE `antibiotic_test`
ADD COLUMN `parent_id` bigint DEFAULT NULL COMMENT '父检测单ID(复检关联)' AFTER `id`,
ADD COLUMN `test_category` varchar(20) NOT NULL DEFAULT 'QUICK' COMMENT '检测类别: QUICK快检, RECHECK复检' AFTER `test_type`;

ALTER TABLE `antibiotic_test`
ADD KEY `idx_parent` (`parent_id`),
ADD KEY `idx_category` (`test_category`);

-- 装车记录表增加运输责任人字段（冗余备份，driver_id已有）
ALTER TABLE `loading_record`
ADD COLUMN `transporter_name` varchar(50) DEFAULT NULL COMMENT '运输责任人姓名' AFTER `driver_id`;

-- 奶罐清洗表增加确认状态
ALTER TABLE `tank_cleaning`
ADD COLUMN `confirm_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '确认状态: PENDING待确认, CONFIRMED已确认' AFTER `check_time`;

ALTER TABLE `tank_cleaning`
ADD KEY `idx_confirm_status` (`confirm_status`);
