<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
</script>

<style>
@import './styles/admin-theme.scss';

/* Reset and Base Styles */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  font-size: 14px;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

html, body, #app {
  height: 100%;
  font-family: 'PingFang SC', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
}

/* Custom Scrollbar */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--admin-bg-light);
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Selection */
::selection {
  background: rgba(14, 165, 233, 0.25);
  color: var(--admin-text);
}

/* Focus Outline */
:focus-visible {
  outline: 2px solid var(--admin-primary);
  outline-offset: 2px;
}

/* Link Styles */
a {
  color: var(--admin-primary);
  text-decoration: none;
  transition: color var(--admin-transition);
}

a:hover {
  color: var(--admin-primary-dark);
}

/* Admin Card Style */
.admin-card {
  background: #fff;
  border-radius: var(--admin-radius-md);
  padding: 24px;
  box-shadow: var(--admin-shadow-sm);
  margin-bottom: 24px;
  border: 1px solid #f1f5f9;
}

/* Table Card */
.table-card {
  background: #fff;
  border-radius: var(--admin-radius-md);
  box-shadow: var(--admin-shadow-sm);
  border: 1px solid #f1f5f9;
  overflow: hidden;
}

/* Page Header */
.page-header {
  margin-bottom: 24px;

  h2 {
    font-size: 24px;
    font-weight: 700;
    color: var(--admin-text);
    margin: 0 0 8px;
    letter-spacing: -0.5px;
  }

  p {
    font-size: 14px;
    color: var(--admin-text-muted);
    margin: 0;
  }
}

/* Stats Card for Dashboard */
.stats-card {
  background: var(--gradient-sky);
  border-radius: var(--admin-radius-md);
  padding: 24px;
  color: #fff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 22px rgba(14, 165, 233, 0.20);

  .stats-value {
    font-size: 32px;
    font-weight: 700;
    margin-bottom: 8px;
  }

  .stats-label {
    font-size: 14px;
    opacity: 0.9;
    font-weight: 500;
  }

  .stats-icon {
    font-size: 48px;
    opacity: 0.2;
    position: absolute;
    right: 24px;
    top: 50%;
    transform: translateY(-50%);
  }
}

/* Glass Card Component */
.glass-card {
  background: rgba(255, 255, 255, 0.80);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.60);
  border-radius: 20px;
  box-shadow:
    0 6px 16px -10px rgba(0, 0, 0, 0.10),
    0 22px 60px -30px rgba(2, 132, 199, 0.35);
  transition: all var(--admin-transition);
}

.glass-card:hover {
  transform: translateY(-2px);
  box-shadow:
    0 10px 20px -12px rgba(0, 0, 0, 0.14),
    0 26px 70px -30px rgba(13, 148, 136, 0.28);
}

/* Gradient Button */
.btn-gradient {
  background: linear-gradient(135deg, var(--admin-primary) 0%, var(--admin-secondary) 100%) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 600;
  border-radius: 50px !important;
  box-shadow: 0 10px 26px -16px rgba(2, 132, 199, 0.65);
  transition: all var(--admin-transition);
}

.btn-gradient:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 36px -18px rgba(2, 132, 199, 0.72);
}

.btn-gradient:active {
  transform: translateY(0);
}

/* Custom Input */
.custom-input .el-input__wrapper {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #e2e8f0;
  border-radius: 50px !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
  padding: 0 20px;
  height: 44px;
  transition: all var(--admin-transition);
}

.custom-input .el-input__wrapper:hover {
  background: #ffffff;
  border-color: #cbd5e1;
}

.custom-input .el-input__wrapper.is-focus {
  background: #ffffff;
  border-color: var(--admin-primary);
  box-shadow: 0 0 0 4px rgba(14, 165, 233, 0.12);
}

/* Status Badges */
.badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 50px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.badge-blue {
  background: var(--gradient-sky);
  color: #fff;
  box-shadow: 0 10px 24px -16px rgba(2, 132, 199, 0.6);
}

.badge-green {
  background: var(--gradient-leaf);
  color: #fff;
  box-shadow: 0 10px 24px -16px rgba(5, 150, 105, 0.55);
}

.badge-orange {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: #fff;
  box-shadow: 0 10px 24px -16px rgba(245, 158, 11, 0.55);
}

.badge-red {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: #fff;
  box-shadow: 0 10px 24px -16px rgba(239, 68, 68, 0.55);
}

/* Table Styles */
.custom-table {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f0f9ff;
}

.custom-table .el-table__header th {
  font-weight: 600;
  color: #475569;
  background: #f8fafc;
  border-bottom: 2px solid #e2e8f0;
  padding: 16px 0;
}

.custom-table .el-table__row td {
  padding: 16px 0;
}

/* Card Header */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.3px;
}

/* Animation */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.animate-fade-in {
  animation: fadeIn 0.4s ease;
}

.animate-slide-up {
  animation: slideUp 0.4s ease;
}

.animate-float {
  animation: float 4s ease-in-out infinite;
}
</style>
