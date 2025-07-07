package com.ureka.team3.utong_backend.auth.util.oauth;


import org.springframework.stereotype.Component;

@Component
public class OAuth2HtmlGenerator {
    
    public String generateSuccessHtml(String accessToken, String tokenType, String expiresIn) {
        return """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>OAuth2 로그인 성공</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        max-width: 600px;
                        margin: 50px auto;
                        padding: 20px;
                        text-align: center;
                        background-color: #f5f5f5;
                    }
                    .success-container {
                        background: white;
                        padding: 40px;
                        border-radius: 10px;
                        box-shadow: 0 4px 15px rgba(0,0,0,0.1);
                    }
                    .success-icon {
                        font-size: 48px;
                        color: #28a745;
                        margin-bottom: 20px;
                    }
                    .loading {
                        display: inline-block;
                        width: 20px;
                        height: 20px;
                        border: 3px solid #f3f3f3;
                        border-top: 3px solid #3498db;
                        border-radius: 50%%;
                        animation: spin 1s linear infinite;
                    }
                    @keyframes spin {
                        0%% { transform: rotate(0deg); }
                        100%% { transform: rotate(360deg); }
                    }
                    .status {
                        margin-top: 20px;
                        font-weight: bold;
                    }
                    .token-info {
                        background: #e9ecef;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                        font-size: 12px;
                        word-break: break-all;
                    }
                </style>
            </head>
            <body>
                <div class="success-container">
                    <div class="success-icon">✓</div>
                    <h1>구글 로그인 성공!</h1>
                    <p>토큰을 저장하고 메인 페이지로 이동합니다...</p>
                    <div class="loading" id="loading"></div>
                    <div class="status" id="status">토큰을 localStorage에 저장 중...</div>
                    
                    <div class="token-info">
                        <div><strong>Token Type:</strong> %s</div>
                        <div><strong>Expires In:</strong> %s ms</div>
                    </div>
                </div>

                <script>
                    console.log('OAuth2 성공 페이지 로드됨');
                    
                    const accessToken = '%s';
                    const statusEl = document.getElementById('status');
                    const loadingEl = document.getElementById('loading');
                    
                    if (accessToken && accessToken !== 'null') {
                        localStorage.setItem('accessToken', accessToken);
                        statusEl.textContent = '✅ 구글 로그인 완료! 토큰이 저장되었습니다.';
                        statusEl.style.color = '#28a745';
                        loadingEl.style.display = 'none';
                        
                        setTimeout(() => {
                            window.location.href = '/';
                        }, 3000);
                        
                    } else {
                        statusEl.textContent = '❌ 토큰을 받지 못했습니다.';
                        statusEl.style.color = '#dc3545';
                        loadingEl.style.display = 'none';
                        
                        setTimeout(() => {
                            window.location.href = '/?error=no_token';
                        }, 3000);
                    }
                </script>
            </body>
            </html>
            """.formatted(
                tokenType != null ? tokenType : "Bearer",
                expiresIn != null ? expiresIn : "3600000",
                accessToken != null ? accessToken : "null"
            );
    }
}