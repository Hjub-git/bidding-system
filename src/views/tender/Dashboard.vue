<template>
  <div class="dashboard">
    <el-container>
      <el-header class="dashboard-header">
        <div class="header-content">
          <h2>招标单位控制台</h2>
          <div>
            <el-button type="text" @click="$router.push('/')">返回首页</el-button>
            <el-button type="text" @click="logout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-main>
        <el-card class="mb-16">
          <h3>欢迎，{{ currentUser ? currentUser.username : '招标用户' }}</h3>
          <p>角色：{{ userRole || 'tender' }}</p>
        </el-card>

        <el-card class="mb-16">
          <div slot="header">发布招标信息</div>
          <el-form :model="form" label-width="120px" @submit.native.prevent>
            <el-form-item label="项目编号"><el-input v-model="form.projectNumber" /></el-form-item>
            <el-form-item label="设备名称"><el-input v-model="form.equipmentName" /></el-form-item>
            <el-form-item label="型号"><el-input v-model="form.model" /></el-form-item>
            <el-form-item label="包装要求"><el-input type="textarea" v-model="form.packagingRequirements" /></el-form-item>
            <el-form-item label="数量"><el-input v-model.number="form.quantity" /></el-form-item>
            <el-form-item label="截止日期"><el-date-picker v-model="form.deadline" type="date" placeholder="选择日期" /></el-form-item>
            <el-form-item label="投标保证金"><el-input v-model.number="form.deposit" /></el-form-item>
            <el-form-item label="描述"><el-input type="textarea" v-model="form.description" /></el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="creating" :disabled="creating" @click="createTender">发布</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card>
          <div slot="header" class="card-header">
            <span>招标信息管理</span>
            <div>
              <el-button size="mini" @click="loadTenders">刷新</el-button>
              <el-button size="mini" type="primary" :disabled="!selectedRow" @click="editSelected">修改选中</el-button>
            </div>
          </div>
          <el-table :data="tenders" style="width:100%" v-if="tenders.length" @selection-change="onSelectionChange">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="project_number" label="项目编号" width="140" />
            <el-table-column prop="equipment_name" label="设备名称" />
            <el-table-column prop="model" label="型号" />
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column prop="deadline" label="截止日期" width="140" />
            <el-table-column label="操作" width="340">
              <template slot-scope="scope">
                <el-button type="primary" size="mini" @click="viewBids(scope.row)">查看投标</el-button>
                <el-button type="warning" size="mini" @click="editTender(scope.row)">修改</el-button>
                <el-button type="danger" size="mini" @click="deleteTender(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-else>暂无招标项目</div>
        </el-card>

        <el-dialog title="投标列表" :visible.sync="bidsDialogVisible" width="800px">
          <el-table :data="bids" style="width:100%">
            <el-table-column prop="manufacturer_name" label="厂家" />
            <el-table-column prop="total_amount" label="总金额" width="120" />
            <el-table-column prop="production_date" label="生产日期" width="140" />
            <el-table-column prop="warranty_period" label="保质期" width="120" />
            <el-table-column prop="deposit_payment_date" label="保证金缴款日期" width="160" />
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column label="操作" width="220">
              <template slot-scope="scope">
                <el-button type="success" size="mini" @click="acceptBid(scope.row.id)">接受</el-button>
                <el-button type="danger" size="mini" @click="rejectBid(scope.row.id)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-dialog>

        <el-dialog title="修改招标" :visible.sync="editDialogVisible" width="600px">
          <el-form :model="editForm" label-width="120px" @submit.native.prevent>
            <el-form-item label="设备名称"><el-input v-model="editForm.equipmentName" /></el-form-item>
            <el-form-item label="型号"><el-input v-model="editForm.model" /></el-form-item>
            <el-form-item label="包装要求"><el-input type="textarea" v-model="editForm.packagingRequirements" /></el-form-item>
            <el-form-item label="数量"><el-input v-model.number="editForm.quantity" /></el-form-item>
            <el-form-item label="截止日期"><el-date-picker v-model="editForm.deadline" type="date" /></el-form-item>
            <el-form-item label="保证金"><el-input v-model.number="editForm.deposit" /></el-form-item>
            <el-form-item label="状态">
              <el-select v-model="editForm.status">
                <el-option label="开放" value="open" />
                <el-option label="关闭" value="closed" />
                <el-option label="取消" value="cancelled" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateTender">保存</el-button>
              <el-button @click="cancelEdit">取消</el-button>
            </el-form-item>
          </el-form>
        </el-dialog>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { tenderApi, bidApi } from '@/services/api'
export default {
  name: 'TenderDashboard',
  data() {
    return {
      form: { projectNumber: '', equipmentName: '', model: '', packagingRequirements: '', quantity: null, deadline: '', deposit: null, description: '' },
      creating: false,
      tenders: [],
      bidsDialogVisible: false,
      bids: [],
      editDialogVisible: false,
      editId: null,
      selectedRow: null,
      editForm: { equipmentName: '', model: '', packagingRequirements: '', quantity: null, deadline: '', deposit: null, status: '' }
    }
  },
  computed: {
    currentUser() { return this.$store.getters.currentUser },
    userRole() { return this.$store.getters.userRole }
  },
  created() {
    this.loadTenders()
  },
  methods: {
    logout() {
      this.$store.dispatch('logout')
      this.$message.success('已退出登录')
      this.$router.push('/')
    },
    resetForm() { this.form = { projectNumber: '', equipmentName: '', model: '', packagingRequirements: '', quantity: null, deadline: '', deposit: null, description: '' } },
    async createTender() {
      // 基本校验
      if (!this.form.projectNumber || !this.form.equipmentName || !this.form.model) {
        this.$message.error('请填写项目编号、设备名称和型号')
        return
      }
      if (!this.form.quantity || this.form.quantity <= 0) {
        this.$message.error('请填写正确的数量')
        return
      }
      if (!this.form.deadline) {
        this.$message.error('请选择截止日期')
        return
      }
      if (this.form.deposit == null || this.form.deposit < 0) {
        this.$message.error('请填写正确的保证金')
        return
      }
      this.creating = true
      try {
        // 后端接口用 snake_case 字段名
        const payload = {
          project_number: this.form.projectNumber,
          equipment_name: this.form.equipmentName,
          model: this.form.model,
          packaging_requirements: this.form.packagingRequirements,
          quantity: this.form.quantity,
          deadline: this.formatDate(this.form.deadline),
          deposit: this.form.deposit,
          description: this.form.description
        }
        await tenderApi.createTender(payload)
        this.$message.success('发布成功')
        this.resetForm()
        this.loadTenders()
      } catch (e) {
        const msg = e.response?.data?.message || e.message || '发布失败'
        if (e.response?.status === 403) {
          this.$message.error('权限不足：请使用招标单位账号登录')
        } else if (e.response?.status === 400 && /招标单位信息不存在/.test(msg)) {
          this.$message.error('招标单位信息不存在：请先完善招标单位资料')
        } else {
          this.$message.error(msg)
        }
        console.error('发布失败', e)
      } finally {
        this.creating = false
      }
    },
    async loadTenders() {
      try {
        // 仅加载当前招标单位的招标项目
        this.tenders = await tenderApi.getMyTenders()
      } catch (e) {
        console.error('加载招标项目失败', e)
        const msg = e.response?.data?.message || e.message || '加载招标项目失败'
        this.$message.error(msg)
      }
    },
    async viewBids(tender) {
      try {
        const res = await bidApi.getBids({ tenderProjectId: tender.id })
        this.bids = res
        this.bidsDialogVisible = true
      } catch (e) { console.error('加载投标失败', e) }
    },
    editTender(tender) {
      this.editId = tender.id
      this.editForm = {
        equipmentName: tender.equipment_name,
        model: tender.model,
        packagingRequirements: tender.packaging_requirements,
        quantity: tender.quantity,
        deadline: tender.deadline,
        deposit: tender.deposit,
        status: tender.status
      }
      this.editDialogVisible = true
    },
    editSelected() {
      if (!this.selectedRow) {
        this.$message.error('请先在列表中选择一个招标项目')
        return
      }
      this.editTender(this.selectedRow)
    },
    onSelectionChange(sel) {
      this.selectedRow = sel && sel.length ? sel[0] : null
    },
    async updateTender() {
      if (!this.editId) {
        this.$message.error('未选择要修改的招标项目')
        return
      }
      try {
        await tenderApi.updateTender(this.editId, {
          equipment_name: this.editForm.equipmentName,
          model: this.editForm.model,
          packaging_requirements: this.editForm.packagingRequirements,
          quantity: this.editForm.quantity,
          deadline: this.formatDate(this.editForm.deadline),
          deposit: this.editForm.deposit,
          status: this.editForm.status
        })
        this.$message.success('已更新')
        this.editDialogVisible = false
        this.editId = null
        this.loadTenders()
      } catch (e) {
        const msg = e.response?.data?.message || e.message || '更新失败'
        this.$message.error(msg)
        console.error('更新失败', e)
      }
    },
    async deleteTender(id) {
      if (!id) {
        this.$message.error('未选择要删除的招标项目')
        return
      }
      try {
        await tenderApi.deleteTender(id)
        this.$message.success('已删除')
        this.loadTenders()
      } catch (e) {
        const msg = e.response?.data?.message || e.message || '删除失败'
        this.$message.error(msg)
        console.error('删除失败', e)
      }
    },
    cancelEdit() {
      this.editDialogVisible = false
      this.editId = null
      this.selectedRow = null
    },
    async acceptBid(id) { try { await bidApi.acceptBid(id); this.$message.success('已接受'); this.bidsDialogVisible=false } catch (e) { console.error('接受失败', e) } },
    async rejectBid(id) { try { await bidApi.rejectBid(id); this.$message.success('已拒绝'); this.bidsDialogVisible=false } catch (e) { console.error('拒绝失败', e) } },
    formatDate(val) {
      if (!val) return ''
      const d = typeof val === 'string' ? new Date(val) : val
      const y = d.getFullYear()
      const m = (d.getMonth() + 1).toString().padStart(2, '0')
      const day = d.getDate().toString().padStart(2, '0')
      return `${y}-${m}-${day}`
    }
  }
}
</script>

<style scoped>
.dashboard-header {
  background-color: #E6A23C;
  color: white;
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mb-16 { margin-bottom: 16px; }
</style>