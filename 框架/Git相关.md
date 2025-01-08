# Git相关

#### 更新本地分支列表和远程同步：

删除的是本地已经跟踪过远程分支，但远程分支被删除的那些本地跟踪分支。

```
git fetch --prune
```

#### 放弃本地修改：

**未使用git add 缓存代码**

- ```
  git checkout -- filepathname 
  ```

- ```
  git checkout .
  ```

**已使用git add 缓存代码，未使用git commit**

- ```
  git reset HEAD filepathname 
  ```

- ```
  git reset HEAD
  ```

**已经用 git commit 提交了代码**

​		回退到上一次commit的状态

- ```
  git reset --hard HEAD^
  ```

  使用git log命令查看git提交历史和commitid

- ```
  git reset --hard commitid
  ```


#### 删除本地分支（需要powershell环境）

```linux
 git branch -D @(git branch | select-string  "test" | Foreach {$_.Line.Trim()}) 
```

