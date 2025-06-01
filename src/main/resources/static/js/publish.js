// 图片预处理函数
function preprocessImage(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = function(e) {
            // 获取 base64 字符串
            let base64 = e.target.result;
            // 移除所有空格和换行符
            base64 = base64.replace(/[\s\n\r]/g, '');
            // 确保是有效的 base64 格式
            if (base64.startsWith('data:image/')) {
                resolve(base64);
            } else {
                reject(new Error('无效的图片格式'));
            }
        };
        reader.onerror = () => reject(new Error('图片读取失败'));
        reader.readAsDataURL(file);
    });
}

// 图片上传处理
function handleImageUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    // 检查文件大小（4MB = 4 * 1024 * 1024 bytes）
    if (file.size > 4 * 1024 * 1024) {
        alert('图片大小不能超过4MB');
        event.target.value = ''; // 清空文件输入
        return;
    }

    // 检查文件类型
    if (!file.type.startsWith('image/')) {
        alert('请上传图片文件');
        event.target.value = '';
        return;
    }

    // 显示上传中提示
    const uploadButton = event.target;
    const originalText = uploadButton.value;
    uploadButton.value = '上传中...';
    uploadButton.disabled = true;

    // 预处理图片
    preprocessImage(file)
        .then(base64 => {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('base64', base64);

            return fetch('http://localhost:8080/api/upload', {
                method: 'POST',
                body: formData,
                headers: {
                    'Authorization': localStorage.getItem('token')
                }
            });
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('上传失败：' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            if (data.code === 200) {
                // 上传成功，显示图片预览
                const imagePreview = document.getElementById('imagePreview');
                imagePreview.src = 'http://localhost:8080' + data.data;
                imagePreview.style.display = 'block';
            } else {
                throw new Error(data.message || '上传失败');
            }
        })
        .catch(error => {
            console.error('上传出错：', error);
            alert(error.message || '上传失败，请重试');
        })
        .finally(() => {
            // 恢复按钮状态
            uploadButton.value = originalText;
            uploadButton.disabled = false;
        });
} 