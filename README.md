# ai-boot

当前项目已按前后端分层完成目录整理：

- 后端：`/api`
- 前端：`/pc`

## 后端启动

```bash
cd api
mvn spring-boot:run
```

## 前端启动

```bash
cd pc
npm install
npm run dev
```

前端开发地址默认是 `http://localhost:5173`，并已通过 Vite 代理转发 `/api` 请求到后端 `http://localhost:8080`。
