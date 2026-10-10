<template>
  <div class="login-container">
    <el-card class="login-card">
      <div slot="header" class="header">
        <h2>机电设备在线招投标系统</h2>
        <h3>用户登录</h3>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm('loginForm')" :loading="loading">登录</el-button>
          <el-button @click="resetForm('loginForm')">重置</el-button>
        </el-form-item>
        <div class="register-link">
          <router-link to="/register">没有账号？立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      loginForm: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          this.loading = true
          try {
            await this.$store.dispatch('login', this.loginForm)
            const role = this.$store.getters.userRole
            
            // 根据角色跳转到不同的页面
            if (role === 'manufacturer') {
              this.$router.push('/manufacturer')
            } else if (role === 'tender') {
              this.$router.push('/tender')
            } else if (role === 'admin') {
              this.$router.push('/admin')
            } else {
              this.$router.push('/')
            }
            
            this.$message.success('登录成功')
          } catch (error) {
            this.$message.error('登录失败: ' + (error.response?.data?.message || '未知错误'))
          } finally {
            this.loading = false
          }
        } else {
          return false
        }
      })
    },
    resetForm(formName) {
      this.$refs[formName].resetFields()
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.login-card {
  width: 450px;
}

.header {
  text-align: center;
}

.register-link {
  text-align: right;
  margin-top: 10px;
  font-size: 14px;
}
</style>