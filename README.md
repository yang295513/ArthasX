# 须知
### 1.首先运行maven打包
> mvn clean install

### 2.然后确定
**arthasX-core.jar**和**spy-1.0-SNAPSHOT.jar**两个jar包路径
### 3.用时需要调整
io.y295513.github.agent.AgentBootstrap.ARTHASX_CORE_PATH 调整为**arthasX-core.jar**的路径
io.y295513.github.core.config.Configure.spyJarPath 调整为**spy-1.0-SNAPSHOT.jar**的路径
这两个路径在target目录下

### 4. 重新运行mvn clean install 命令打包

命令格式为
watch io.y295513.github.boot.controller.TestController hello

# 接下来的计划

- [x] 实现watch命令
- [ ] 支持cglib增强
- [ ] 支持增强还原
- [ ] 支持多租户
- [ ] 实现WEB端页面，简化命令行带来的繁琐，简化操作
- [ ] 实现热替换
- [ ] 未完续
# Q&A
### mvn重新打包失败报Failed to clean project: failed to delete
是因为agent被挂载到了目标应用内，无法清理导致的，可以将目标应用停掉
### 为什么有很多bug
目前版本只是个技术验证，所有的代码都是该目的，故存在大量bug和不合理设计