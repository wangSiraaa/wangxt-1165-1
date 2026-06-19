-- 初始化默认用户（密码由DataInitializer在启动时使用BCrypt动态生成，默认密码：123456）

-- 初始化牧场数据
INSERT INTO pasture (id, pasture_code, pasture_name, address, contact_person, contact_phone, status, create_time, update_time, deleted) VALUES
(1, 'MC001', '一号牧场', '北京市朝阳区奶牛养殖基地1号', '张场长', '13900000001', 1, NOW(), NOW(), 0),
(2, 'MC002', '二号牧场', '北京市海淀区奶牛养殖基地2号', '李场长', '13900000002', 1, NOW(), NOW(), 0);

-- 初始化奶罐数据
INSERT INTO milk_tank (id, tank_code, tank_name, capacity, current_volume, tank_status, pasture_id, current_batch_id, last_clean_time, create_time, update_time, deleted) VALUES
(1, 'NG001', '1号奶罐', 5000.00, 0, 'IDLE', 1, NULL, NOW(), NOW(), NOW(), 0),
(2, 'NG002', '2号奶罐', 5000.00, 0, 'IDLE', 1, NULL, NOW(), NOW(), NOW(), 0),
(3, 'NG003', '3号奶罐', 8000.00, 0, 'IDLE', 2, NULL, NOW(), NOW(), NOW(), 0),
(4, 'NG004', '4号奶罐', 8000.00, 0, 'IDLE', 2, NULL, NOW(), NOW(), NOW(), 0);

-- 初始化奶罐清洗记录（标记奶罐已清洗）
INSERT INTO tank_cleaning (id, cleaning_no, tank_id, pasture_id, operator_id, cleaning_start_time, cleaning_end_time, cleaning_method, cleaning_agent, water_temperature, is_qualified, checker_id, check_time, remark, create_time, update_time, deleted) VALUES
(1, 'QX20240601001', 1, 1, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), '高温消毒', '食品级清洗剂', 85.00, 1, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), '首次清洗合格', NOW(), NOW(), 0),
(2, 'QX20240601002', 2, 1, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), '高温消毒', '食品级清洗剂', 85.00, 1, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), '首次清洗合格', NOW(), NOW(), 0),
(3, 'QX20240601003', 3, 2, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), '高温消毒', '食品级清洗剂', 85.00, 1, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), '首次清洗合格', NOW(), NOW(), 0),
(4, 'QX20240601004', 4, 2, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), '高温消毒', '食品级清洗剂', 85.00, 1, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), '首次清洗合格', NOW(), NOW(), 0);
