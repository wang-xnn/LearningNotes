##### 1. git基础命令

沙盘小游戏：Git sandbox-https://learngitbranching.js.org/?locale=zh_CN

###### 1.1 git commit

git commit 从index暂存区直接存入本地仓库

```bash
$ git commit -m "update file"
```

###### 1.2  创建、删除和转换分支

```bash
#创建分支以及转换分支
$ git branch <name> #创建新分支,仍在旧分支
$ git checkout -b <name> #创建并到新分支
$ git checkout <name> #到该分支
#列出分支
$ git branch #列出本地分支
$ git branch -r #列出所有远程分支
#删除分支
$ git branch -d <name> #删除本地分支
#删除远程分支                                
$ git push origin --delete <branch-name>
$ git branch -dr <remote/branch>
```

###### 1.3 合并分支

```bash
$ git merge <branch-name> #合并这个name的分支到本分支
$ git rebase <branch-name> #合并本分支到这个name的分支
#merge 和rebase实际差不多，可能只是合并的主分支有点区别，但每个合并分支都有2个父分支，差别非常小
```



##### 2. HEAD和相对引用、撤销

HEAD 是一个对当前检出记录的符号引用，具有指向，由`git checkout <name>`控制

每个分支都有一个哈希值，可以通过`git log`查看哈希值，来指定特别的分支，只需要提供能够哈希值提交记录的前几个字符即可

###### 2.1 相对引用

```bash
# ^标志和~标志
$ git checkout main^ #^不带数字，head指向上一个分支
$ git checkout main^[1/2] #这是对于合并分支来说，可以通过^1或^2指向两个父分支中的一个` |
$ git checkout main~<num> #head向上移动num个分支`            |
# ^ 和 ~可以组合起来，如git checkout main^^2~3 ---head指向main分支的上一个分支的第二个父节点的往上数第3个分支 
# 并且不单单是移动head,还可以修改分支的位置，与下个命令结合    |
$ git branch -f main HEAD~3 修改main分支的位置到head指向的分支的上面第3个分支
```

###### 2.2 撤销分支

```bash
#reset 和 revert命令
$ git reset HEAD~1
$ git revert HEAD
#reset适用于本地分支，但对远程分享的分支无效，在本地直接撤销，并回到上一个位置，没有head最初所在的分支了 
# ------------------------------------------------------------ 
#为了撤销更改并分享给别人，revert是直接增加一个新的分支，这个分支位于head下方第一个，并且与head上方第一个分支提交记录相同 |
```



##### 3. 自由修改提交树

```bash
$ git cherry-pick main~1 main~2 main~3
$ git rebase -i HEAD~4
# cherry-pick将 main~1 main~2 main~3依次放入head下方分支 
# git rebase -i 调整head和head上方三个的顺序或者可以删除该分支
```



##### 4. 删除远程仓库文件

删除本地文件

```bash
git rm a.text #删除文件
git rm -r a #删除文件夹
```

如果修改.gitignore时考虑不周，远程仓库有不需要展示的文件

```bash
$ git rm -r  -n --cached 文件名/文件夹  #这不会删除任何文件，会展示要删除的文件，去掉-n就是删除
$ git rm -r --cached 文件名/文件夹
```

接着，再修改本地 .gitignore文件，然后提交到本地仓库和远程仓库

注意：如果不加`--cached`,直接从工作区和暂存区删除文件，但加了`--cached`,从暂存区删除文件，工作区保留



##### 5. 修改远程仓库文件名

分2次提交，第一次添加新文件夹，第二次删除旧文件夹？

```bash
$ git mv --force AppTest.java  apptest.java  #旧文件 新文件
$ git add apptest.java  #可以加个-u
$ git commit –m "rename"
$ git push origin master
# --force 可以变为-f
```



##### 6. git log日志查看

**`git log`**

直接使用git log

```bash
$ git log
commit 3ba5f355fc461f5a6496fa8133953ecf58fa5c14 (HEAD -> main, origin/main, origin/HEAD)
Author: wangx <252279128@qq.com>
Date:   Thu Jul 15 10:47:17 2021 +0800

    rm sandbox

commit f8d91ae4e331faadee1048e966ec93435cd73f67
Author: wangx <252279128@qq.com>
Date:   Thu Jul 15 10:11:17 2021 +0800

    2021/07/15 leetcode每日一题
...
```

参数 `--oneline  --graph  --reverse  -number  --author  --since  --before`

```bash
# --oneline 查看历史记录的简洁版本
$ git log --oneline
3ba5f35 (HEAD -> main, origin/main, origin/HEAD) rm sandbox
f8d91ae 2021/07/15 leetcode每日一题
671f828 springboot-03-web
4801a73 2021/07/14 leetcode每日一题 二分
# --graph 查看分支  前面增加了符号
$ git log --oneline --graph
* 3ba5f35 (HEAD -> main, origin/main, origin/HEAD) rm sandbox
* f8d91ae 2021/07/15 leetcode每日一题
* 671f828 springboot-03-web
* 4801a73 2021/07/14 leetcode每日一题 二分
#--reverse 逆向显示所有日志
$ git log --oneline --reverse
# -5  显示前5条日志
# --author 查找指定的用户
$ git log --oneline --author=wangxnn
3ba5f35 (HEAD -> main, origin/main, origin/HEAD) rm sandbox
f8d91ae 2021/07/15 leetcode每日一题
671f828 springboot-03-web
4801a73 2021/07/14 leetcode每日一题 二分
#--since --before 查找指定日期  --util --after
$ git log --oneline --before={2.days.ago} --after={2021-07-02}
2d83e61 2021/07/12 leetcode每日一题
a89a108 2021/07/11 leetcode周赛/每日一题
349c50b 2021/07/10 leetcode 设计数据结构 981“
```

`git blame` 查找指定文件的修改记录，好像无法查找目录

```bash
$ git blame README.md
^0a54c4a (star wx 2021-07-06 19:28:27 +0800 1) # git-study
0c2c6826 (wangx   2021-07-07 20:25:05 +0800 2) 初次学习git
0c2c6826 (wangx   2021-07-07 20:25:05 +0800 3)
0c2c6826 (wangx   2021-07-07 20:25:05 +0800 4) 学习的课程为b站狂神讲git
0c2c6826 (wangx   2021-07-07 20:25:05 +0800 5) b站链接：https://www.bilibili.com/video/BV1FE411P7B3?from=search&seid=10353468027444860746
```

git修改未push已提交的commit

```bash
$ git commit --amend
```

git修改最近一次已经push的commit

```bash 
$ git commit --amend  //修改commit后
$ git push -f         //强制提交
```

git撤销commit

```bash
$ git reset --soft HEAD~1 //不删除工作空间改动代码，撤销commit，不撤销git add . 
$ git reset --hard HEAD~1 //删除工作空间改动代码，撤销commit，撤销git add . 
$ git reset --mixed HEAD~1//不删除工作空间改动代码，撤销commit，并且撤销git add . 操作,不加mixed一样，这是默认选项
```



##### 7.  git 报错

###### 碰到问题1

Git 无法连接到远程仓库

```bash
$ git push
fatal: unable to access 'https://github.com/wang-xn/git-study.git/': OpenSSL SSL_read: Connection was reset, errno 10054
```

错误原因可能是网络不稳定，连接超时造成的，可以先多次尝试，如果你试了多次还是报这个错误，建议执行下面的命令 

```bash
$ git config --global http.sslVerify "false"
```

这个命令用来修改设置，解除 SSL 验证。

###### 碰到问题2

git clone项目失败

主要是2个问题，一个是连接超时，一个是openssl连接失败

```bash
$ git clone https://github.com/wang-xnn/LearningNotes.git
Cloning into 'LearningNotes'...
fatal: unable to access 'https://github.com/wang-xnn/LearningNotes.git/': OpenSSL SSL_connect: Connection was reset in connection to github.com:443
```

```bash
fatal: unable to access 'https://github.com/wang-xnn/learningnotes.git/': failed to connect to github.com port 443 after 21091 ms: timed out
```

网上的解答是可能是vpn代理出现问题，故尝试修改全局设置

**尝试方案1：**

先设置全局代理，再取消全局代理

```bash
#设置全局代理
git config --global http.proxy 127.0.0.1:7890
git config --global https.proxy 127.0.0.1:7890
#取消全局代理
git config --global unset http.proxy
git config --global unset https.proxy
```

==失败==

**尝试方案二：**

修改git config其他设置

```bash
git config --global http.Backend "openssl"
git config --global http.sslCAInfo "D:\enviroments\Git\mingw64\ssl\cert.pem"
```

==失败==

**尝试方案三：**

修改Clash代理节点，更改全局模式

==失败==

**尝试方案四：**

将https更改为git

```bash
#原先
git clone https://github.com/wang-xnn/LearningNotes.git
#更改后
git clone git://github.com/wang-xnn/LearningNotes.git
```

==成功==。。。

查看git 设置的命令

```bash
git config -l
git config --global --list
git config --global -l  #查看git所有的全局设置
git config --global http.proxy #查看git 特定项的全局设置
git config --global https.proxy
```

```bash
$ git config --global -l
http.sslbackend=openssl
http.sslcainfo=D:\enviroments\Git\mingw64\ssl\cert.pem
http.sslverify=false
user.name=wangxin
user.email=252279128@qq.com
```

