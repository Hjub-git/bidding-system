﻿<template>
  <div class="home-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="logo">机电设备在线招投标系统</div>
          <div class="nav-links">
            <template v-if="isLoggedIn">
              <el-button v-if="userRole === 'admin'" type="text" @click="goToDashboard">进入系统</el-button>
              <el-button v-if="userRole === 'manufacturer'" type="text" @click="goToMyBids">我的投标</el-button>
              <el-button v-if="userRole === 'tender'" type="text" @click="handlePublishTender">发布招标</el-button>
              <el-button type="text" @click="goMessages">留言板</el-button>
              <el-button type="text" @click="logout">退出登录</el-button>
            </template>
            <template v-else>
              <el-button type="text" @click="$router.push('/login')">登录</el-button>
              <el-button type="text" @click="$router.push('/register')">注册</el-button>
              <el-button type="text" @click="goMessages">留言板</el-button>
            </template>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <el-row :gutter="20">
          <el-col :span="16">
            <el-card class="welcome-card">
              <h1>欢迎使用机电设备在线招投标系统</h1>
              <p>本系统为设备生产企业与招标单位提供便捷的在线招投标服务。</p>
              
              <el-divider></el-divider>
              
              <h2>最新招标项目</h2>
              <el-form :inline="true" :model="tenderFilters" class="tender-filter-bar" @submit.native.prevent>
                <el-form-item label="关键字">
                  <el-input
                    v-model="tenderFilters.keyword"
                    placeholder="项目编号/设备/型号/单位"
                    clearable
                    style="width: 260px"
                    @keyup.enter.native="searchTenders"
                    @clear="searchTenders"
                  />
                </el-form-item>
                <el-form-item label="状态">
                  <el-select v-model="tenderFilters.status" placeholder="请选择状态" style="width: 160px" @change="onStatusChange">
                    <el-option label="开放中" value="open" />
                    <el-option label="已截止" value="closed" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="searchTenders">搜索</el-button>
                  <el-button @click="resetTenderFilters">重置</el-button>
                </el-form-item>
              </el-form>
              <el-table :data="tenders" style="width: 100%" v-loading="loadingTenders">
                <el-table-column prop="project_number" label="项目编号" width="120"></el-table-column>
                <el-table-column prop="equipment_name" label="设备名称"></el-table-column>
                <el-table-column prop="model" label="型号" width="120"></el-table-column>
                <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
                <el-table-column prop="deadline" label="截止日期" width="120"></el-table-column>
                <el-table-column label="操作" width="200">
                  <template slot-scope="scope">
                    <el-button type="text" size="small" @click="viewTenderDetails(scope.row)">查看详情</el-button>
                    <el-divider direction="vertical"></el-divider>
                    <el-button type="text" size="small" @click="openBid(scope.row)">立即投标</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div style="display:flex; justify-content: space-between; align-items:center; margin-top: 12px;">
                <div style="color:#909399;">共 {{ pagination.total }} 条，当前第 {{ pagination.page }} 页</div>
                <el-pagination
                  background
                  layout="prev, pager, next, sizes, total"
                  :total="pagination.total"
                  :current-page="pagination.page"
                  :page-size="pagination.pageSize"
                  :page-sizes="[5, 10, 20]"
                  @current-change="handlePageChange"
                  @size-change="handleSizeChange"
                />
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card class="info-card">
              <h3>用户指南</h3>
              <div class="guide-links">
                <p>
                  <el-link type="primary">如何注册账号</el-link>
                  <span class="guide-desc">点击首页右上角“注册”，填写用户名、邮箱和密码完成注册；登录后可在“个人中心”完善企业信息与联系人。</span>
                </p>
                <p>
                  <el-link type="primary">如何发布招标信息</el-link>
                  <span class="guide-desc">使用招标单位账号登录，进入“控制台/发布招标”，填写项目编号、设备名称、型号、数量、截止日期等信息后提交。</span>
                </p>
                <p>
                  <el-link type="primary">如何参与投标</el-link>
                  <span class="guide-desc">制造商账号登录后在首页或招标列表中选择目标项目，点击“立即投标”，填写投标总金额、生产日期、质保期等信息后提交。</span>
                </p>
                <p>
                  <el-link type="primary">常见问题解答</el-link>
                  <span class="guide-desc">如无法投标或看不到项目，请确认账号角色与状态是否正常；如需导入项目或投标文档模板，请联系管理员获取最新规范。</span>
                </p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
      
      <el-footer>
        <div class="footer-content">
          <p>© 2025 </p>
        
        </div>
      </el-footer>
    </el-container>
    
    <!-- 招标项目详情对话框 -->
    <el-dialog title="招标项目详情" :visible.sync="dialogVisible" width="60%">
      <div v-if="currentTender">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="项目编号">{{ currentTender.project_number }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ currentTender.equipment_name }}</el-descriptions-item>
          <el-descriptions-item label="型号">{{ currentTender.model }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ currentTender.quantity }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ currentTender.deadline }}</el-descriptions-item>
          <el-descriptions-item label="投标保证金">{{ currentTender.deposit }}</el-descriptions-item>
          <el-descriptions-item label="包装要求" :span="2">{{ currentTender.packaging_requirements }}</el-descriptions-item>
          <el-descriptions-item label="项目描述" :span="2">{{ currentTender.description }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToLogin" v-if="!isLoggedIn">登录参与投标</el-button>
        <el-button type="primary" v-if="isLoggedIn" @click="openBid(currentTender)">立即投标</el-button>
      </span>
    </el-dialog>

    <!-- 提交投标对话框 -->
    <el-dialog title="提交投标" :visible.sync="bidDialogVisible" width="40%">
      <div v-if="selectedTender">
        <el-form :model="bidForm" label-width="120px" @submit.native.prevent>
          <el-form-item label="投标总金额">
            <el-input v-model.number="bidForm.total_amount" placeholder="请输入总金额" />
          </el-form-item>
          <el-form-item label="生产日期">
            <el-date-picker v-model="bidForm.production_date" type="date" value-format="yyyy-MM-dd" placeholder="请选择日期" />
          </el-form-item>
          <el-form-item label="质保期">
            <el-input v-model="bidForm.warranty_period" placeholder="如：24 个月" />
          </el-form-item>
          <el-form-item label="保证金缴纳日期">
            <el-date-picker v-model="bidForm.deposit_payment_date" type="date" value-format="yyyy-MM-dd" placeholder="请选择日期" />
          </el-form-item>
          <el-form-item label="投标文件链接">
            <el-input v-model="bidForm.bid_document" placeholder="可选，提供网盘或链接" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submittingBid" :disabled="submittingBid" @click="submitBid">提交</el-button>
            <el-button @click="bidDialogVisible=false">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { tenderApi } from '@/services/api'
import { bidApi } from '@/services/api'
export default {
  name: 'Home',
  data() {
    return {
      dialogVisible: false,
      currentTender: null,
      tenders: [],
      loadingTenders: false,
      pagination: { page: 1, pageSize: 5, total: 0 },
      tenderFilters: { keyword: '', status: 'open' },
      bidDialogVisible: false,
      selectedTender: null,
      submittingBid: false,
      bidForm: {
        total_amount: null,
        production_date: '',
        warranty_period: '',
        deposit_payment_date: '',
        bid_document: ''
      },
      notices: []
    }
  },
  computed: {
    isLoggedIn() {
      // 修正：使用已定义的 getter isAuthenticated
      return this.$store.getters.isAuthenticated
    },
    userRole() {
      return this.$store.getters.userRole
    }
  },
  async mounted() {
    try {
      await this.loadTenders()
    } catch (e) {
      console.error('首页加载招标失败', e)
      this.$message.error(e.response?.data?.message || '加载招标列表失败')
    }
  },
  methods: {
    normalizeListResponse(res) {
      // 后端分页统一返回 { items, total }
      if (Array.isArray(res)) {
        return { rows: res, total: res.length }
      }
      if (res && Array.isArray(res.items)) {
        return { rows: res.items, total: Number(res.total) || res.items.length || 0 }
      }
      if (res && Array.isArray(res.data)) {
        return { rows: res.data, total: Number(res.total) || res.data.length || 0 }
      }
      return { rows: [], total: 0 }
    },
    async loadTenders() {
      this.loadingTenders = true
      try {
        const res = await tenderApi.getTenders({
          keyword: this.tenderFilters.keyword || undefined,
          status: this.tenderFilters.status || undefined,
          page: this.pagination.page,
          pageSize: this.pagination.pageSize
        })
        const { rows, total } = this.normalizeListResponse(res)
        this.tenders = rows
        this.pagination.total = total || 0
      } finally {
        this.loadingTenders = false
      }
    },
    async searchTenders() {
      try {
        this.pagination.page = 1
        await this.loadTenders()
      } catch (e) {
        console.error('搜索招标失败', e)
        this.$message.error(e.response?.data?.message || '搜索失败')
      }
    },
    async onStatusChange() {
      this.pagination.page = 1
      await this.loadTenders()
    },
    async resetTenderFilters() {
      this.tenderFilters = { keyword: '', status: 'open' }
      try {
        this.pagination.page = 1
        await this.loadTenders()
      } catch (e) {
        console.error('重置筛选失败', e)
      }
    },
    async handlePageChange(p) {
      this.pagination.page = p
      await this.loadTenders()
    },
    async handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.page = 1
      await this.loadTenders()
    },
    viewTenderDetails(tender) {
      this.currentTender = tender
      this.dialogVisible = true
    },
    goToLogin() {
      this.dialogVisible = false
      this.$router.push('/login')
    },
    goToDashboard() {
      if (this.userRole === 'manufacturer') {
        this.$router.push('/manufacturer')
      } else if (this.userRole === 'tender') {
        this.$router.push('/tender')
      } else if (this.userRole === 'admin') {
        this.$router.push('/admin')
      }
    },
    goToMyBids() {
      // 厂家查看“我的投标”
      this.$router.push('/manufacturer')
    },
    handlePublishTender() {
      if (this.userRole === 'tender') {
        this.$router.push('/tender')
      } else {
        this.$message.error('请使用招标单位账号登录后发布招标')
      }
    },
    goMessages() {
      this.$router.push('/messages')
    },
    openBid(tender) {
      if (!this.isLoggedIn) {
        this.$message.info('请先登录后再投标')
        this.$router.push('/login')
        return
      }
      if (this.userRole !== 'manufacturer') {
        this.$message.error('只有厂商角色可以参与投标')
        return
      }
      this.selectedTender = tender
      this.bidDialogVisible = true
    },
    async submitBid() {
      if (!this.selectedTender) return
      if (!this.bidForm.total_amount || this.bidForm.total_amount <= 0) {
        this.$message.error('请填写有效的投标总金额')
        return
      }
      this.submittingBid = true
      try {
        const payload = {
          tender_project_id: this.selectedTender.id,
          total_amount: this.bidForm.total_amount,
          production_date: this.bidForm.production_date || null,
          warranty_period: this.bidForm.warranty_period || null,
          deposit_payment_date: this.bidForm.deposit_payment_date || null,
          bid_document: this.bidForm.bid_document || null
        }
        await bidApi.createBid(payload)
        this.$message.success('投标提交成功')
        this.bidDialogVisible = false
        this.bidForm = { total_amount: null, production_date: '', warranty_period: '', deposit_payment_date: '', bid_document: '' }
      } catch (e) {
        const msg = e.response?.data?.message || e.message || '提交投标失败'
        this.$message.error(msg)
        console.error('提交投标失败', e)
      } finally {
        this.submittingBid = false
      }
    },
    logout() {
      this.$store.dispatch('logout')
      this.$message.success('已退出登录')
      this.$router.push('/')
    }
  }
}
</script>




