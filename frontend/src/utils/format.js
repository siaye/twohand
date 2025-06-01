/**
 * 格式化时间
 * @param {string|number|Date} time 时间
 * @param {string} format 格式化模式，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(time) {
  if (!time) return '暂无数据';
  try {
    // 尝试解析多种日期时间格式，包括后端LocalDateTime.toString()的格式
    let date;
    if (typeof time === 'string') {
      // 尝试解析 ISO 8601 格式 (如 "2023-10-27T10:30:00")
      date = new Date(time.replace('T', ' ')); // 将 T 替换为空格，以便兼容解析
      if (isNaN(date.getTime())) {
        // 如果解析失败，尝试直接创建 Date 对象
        date = new Date(time);
      }
    } else {
      date = new Date(time);
    }

    if (isNaN(date.getTime())) return '暂无数据';

    // 使用 toLocaleString 进行本地化格式化
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    });
  } catch (error) {
    console.error('时间格式化错误:', error);
    return '暂无数据';
  }
}

/**
 * 格式化金额
 * @param {number} amount 金额
 * @param {number} decimals 小数位数，默认为 2
 * @returns {string} 格式化后的金额字符串
 */
export function formatAmount(amount, decimals = 2) {
  if (typeof amount !== 'number') {
    console.warn('Invalid amount:', amount)
    return '0.00'
  }
  
  return amount.toFixed(decimals)
}

/**
 * 格式化文件大小
 * @param {number} bytes 字节数
 * @param {number} decimals 小数位数，默认为 2
 * @returns {string} 格式化后的文件大小字符串
 */
export function formatFileSize(bytes, decimals = 2) {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(decimals)) + ' ' + sizes[i]
}

/**
 * 格式化手机号
 * @param {string} phone 手机号
 * @returns {string} 格式化后的手机号字符串
 */
export function formatPhone(phone) {
  if (!phone) return ''
  
  // 移除所有非数字字符
  const cleaned = phone.replace(/\D/g, '')
  
  // 检查是否是有效的手机号
  if (cleaned.length !== 11) {
    console.warn('Invalid phone number:', phone)
    return phone
  }
  
  // 格式化为 3-4-4 格式
  return cleaned.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3')
}

/**
 * 格式化日期
 * @param {string|Date} date 日期
 * @param {string} format 格式
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

// 格式化金额
export const formatPrice = (price) => {
  if (!price) return '0.00'
  return Number(price).toFixed(2)
} 