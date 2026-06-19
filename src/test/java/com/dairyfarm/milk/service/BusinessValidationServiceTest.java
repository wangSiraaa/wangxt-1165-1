package com.dairyfarm.milk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dairyfarm.milk.common.context.UserContext;
import com.dairyfarm.milk.common.enums.ApprovalStatusEnum;
import com.dairyfarm.milk.common.enums.BatchStatusEnum;
import com.dairyfarm.milk.common.enums.TankStatusEnum;
import com.dairyfarm.milk.common.enums.TestResultEnum;
import com.dairyfarm.milk.common.exception.BusinessException;
import com.dairyfarm.milk.config.TestConfig;
import com.dairyfarm.milk.entity.*;
import com.dairyfarm.milk.mapper.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@Transactional
@Rollback
public class BusinessValidationServiceTest {

    @Autowired
    private BusinessValidationService businessValidationService;

    @Autowired
    private MilkTankMapper milkTankMapper;

    @Autowired
    private MilkBatchMapper milkBatchMapper;

    @Autowired
    private AntibioticTestMapper antibioticTestMapper;

    @Autowired
    private TankCleaningMapper tankCleaningMapper;

    @Autowired
    private PastureMapper pastureMapper;

    private Pasture testPasture;
    private MilkTank testTank;
    private MilkBatch testBatch;
    private AntibioticTest testAntibioticTest;

    @Before
    public void setUp() {
        UserContext userContext = new UserContext();
        userContext.setUserId(1L);
        userContext.setUsername("testUser");
        userContext.setRoleCode("ADMIN");
        userContext.setPastureId(1L);
        UserContext.set(userContext);

        testPasture = new Pasture();
        testPasture.setPastureCode("TEST_PASTURE_001");
        testPasture.setPastureName("测试牧场");
        testPasture.setAddress("测试地址");
        testPasture.setContactPerson("测试联系人");
        testPasture.setContactPhone("13800138000");
        testPasture.setStatus(1);
        pastureMapper.insert(testPasture);
    }

    @After
    public void tearDown() {
        UserContext.remove();
    }

    private MilkTank createMilkTank(String status) {
        MilkTank tank = new MilkTank();
        tank.setTankCode("TANK_" + System.currentTimeMillis());
        tank.setTankName("测试奶罐");
        tank.setCapacity(new BigDecimal("5000.00"));
        tank.setCurrentVolume(new BigDecimal("0.00"));
        tank.setTankStatus(status);
        tank.setPastureId(testPasture.getId());
        milkTankMapper.insert(tank);
        return tank;
    }

    private MilkBatch createMilkBatch(MilkTank tank, String status) {
        MilkBatch batch = new MilkBatch();
        batch.setBatchNo("BATCH_" + System.currentTimeMillis());
        batch.setPastureId(testPasture.getId());
        batch.setTankId(tank.getId());
        batch.setMilkVolume(new BigDecimal("2000.00"));
        batch.setMilkTemperature(new BigDecimal("4.0"));
        batch.setCowCount(50);
        batch.setBatchStatus(status);
        batch.setMilkingStartTime(LocalDateTime.now().minusHours(2));
        batch.setMilkingEndTime(LocalDateTime.now().minusHours(1));
        milkBatchMapper.insert(batch);
        return batch;
    }

    private AntibioticTest createAntibioticTest(MilkBatch batch, String testResult, String approveStatus, Integer isLocked) {
        AntibioticTest test = new AntibioticTest();
        test.setTestNo("TEST_" + System.currentTimeMillis());
        test.setBatchId(batch.getId());
        test.setTankId(batch.getTankId());
        test.setPastureId(testPasture.getId());
        test.setLabId(1L);
        test.setTestType("青霉素类");
        test.setTestMethod("快检卡");
        test.setTestResult(testResult);
        test.setTestValue("阴性");
        test.setTestTime(LocalDateTime.now());
        test.setIsLocked(isLocked);
        test.setApproveStatus(approveStatus);
        antibioticTestMapper.insert(test);
        return test;
    }

    private TankCleaning createTankCleaning(MilkTank tank, boolean isQualified) {
        TankCleaning cleaning = new TankCleaning();
        cleaning.setCleaningNo("CLEAN_" + System.currentTimeMillis());
        cleaning.setTankId(tank.getId());
        cleaning.setPastureId(testPasture.getId());
        cleaning.setCleaningStartTime(LocalDateTime.now().minusHours(3));
        cleaning.setCleaningEndTime(LocalDateTime.now().minusHours(2).plusMinutes(30));
        cleaning.setCleaningMethod("CIP清洗");
        cleaning.setCleaningAgent("碱性清洗剂");
        cleaning.setWaterTemperature(new BigDecimal("85.0"));
        cleaning.setIsQualified(isQualified ? 1 : 0);
        cleaning.setCheckerId(1L);
        cleaning.setCheckTime(LocalDateTime.now().minusHours(2));
        tankCleaningMapper.insert(cleaning);
        return cleaning;
    }

    @Test
    public void test_快检未通过不能装车() {
        testTank = createMilkTank(TankStatusEnum.TESTED.getCode());
        testBatch = createMilkBatch(testTank, BatchStatusEnum.TEST_FAILED.getCode());
        testAntibioticTest = createAntibioticTest(testBatch, TestResultEnum.FAILED.getCode(), ApprovalStatusEnum.APPROVED.getCode(), 0);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            businessValidationService.validateTestResultForLoading(testBatch.getId());
        });

        assertThat(exception.getMessage()).contains("检测不合格");
    }

    @Test
    public void test_检测审批中不能装车() {
        testTank = createMilkTank(TankStatusEnum.TESTED.getCode());
        testBatch = createMilkBatch(testTank, BatchStatusEnum.PENDING_TEST.getCode());
        testAntibioticTest = createAntibioticTest(testBatch, TestResultEnum.PASSED.getCode(), ApprovalStatusEnum.PENDING.getCode(), 0);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            businessValidationService.validateTestResultForLoading(testBatch.getId());
        });

        assertThat(exception.getMessage()).contains("审批中");
    }

    @Test
    public void test_同一奶罐未清洗不能接新批次() {
        testTank = createMilkTank(TankStatusEnum.LOADED.getCode());
        testBatch = createMilkBatch(testTank, BatchStatusEnum.LOADED.getCode());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            businessValidationService.validateTankForNewBatch(testTank.getId(), testPasture.getId());
        });

        assertThat(exception.getMessage()).contains("未清洗");
    }

    @Test
    public void test_奶罐已清洗可以接新批次() {
        testTank = createMilkTank(TankStatusEnum.IDLE.getCode());
        createTankCleaning(testTank, true);

        businessValidationService.validateTankForNewBatch(testTank.getId(), testPasture.getId());
    }

    @Test
    public void test_装车后检测结果不能改为合格() {
        testTank = createMilkTank(TankStatusEnum.LOADED.getCode());
        testBatch = createMilkBatch(testTank, BatchStatusEnum.LOADED.getCode());
        testAntibioticTest = createAntibioticTest(testBatch, TestResultEnum.FAILED.getCode(), ApprovalStatusEnum.APPROVED.getCode(), 1);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            businessValidationService.validateTestResultChange(testAntibioticTest.getId(), TestResultEnum.PASSED.getCode());
        });

        assertThat(exception.getMessage()).contains("已装车");
    }

    @Test
    public void test_未装车的检测结果可以修改() {
        testTank = createMilkTank(TankStatusEnum.TESTED.getCode());
        testBatch = createMilkBatch(testTank, BatchStatusEnum.TEST_PASSED.getCode());
        testAntibioticTest = createAntibioticTest(testBatch, TestResultEnum.FAILED.getCode(), ApprovalStatusEnum.PENDING.getCode(), 0);

        businessValidationService.validateTestResultChange(testAntibioticTest.getId(), TestResultEnum.PASSED.getCode());
    }

    @Test
    public void test_审批驳回不能装车() {
        testTank = createMilkTank(TankStatusEnum.TESTED.getCode());
        testBatch = createMilkBatch(testTank, BatchStatusEnum.TEST_FAILED.getCode());
        testAntibioticTest = createAntibioticTest(testBatch, TestResultEnum.PASSED.getCode(), ApprovalStatusEnum.REJECTED.getCode(), 0);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            businessValidationService.validateTestResultForLoading(testBatch.getId());
        });

        assertThat(exception.getMessage()).contains("已被驳回");
    }

    @Test
    public void test_未检测不能装车() {
        testTank = createMilkTank(TankStatusEnum.PENDING_TEST.getCode());
        testBatch = createMilkBatch(testTank, BatchStatusEnum.PENDING_TEST.getCode());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            businessValidationService.validateTestResultForLoading(testBatch.getId());
        });

        assertThat(exception.getMessage()).contains("尚未进行抗生素检测");
    }

    @Test
    public void test_奶罐状态不是空闲不能接新批次() {
        testTank = createMilkTank(TankStatusEnum.LOADING.getCode());
        createTankCleaning(testTank, true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            businessValidationService.validateTankForNewBatch(testTank.getId(), testPasture.getId());
        });

        assertThat(exception.getMessage()).contains("不能接收新批次");
    }

    @Test
    public void test_批次已装车不能重复装车() {
        testTank = createMilkTank(TankStatusEnum.LOADED.getCode());
        testBatch = createMilkBatch(testTank, BatchStatusEnum.LOADED.getCode());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            businessValidationService.validateBatchStatusForLoading(testBatch.getId());
        });

        assertThat(exception.getMessage()).contains("不能重复装车");
    }
}
