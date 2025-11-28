#!/bin/bash
# 设置Java 21环境变量

# 查找并设置Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

if [ $? -eq 0 ]; then
    echo "已设置 JAVA_HOME 为 Java 21"
    echo "JAVA_HOME: $JAVA_HOME"
    echo ""
    $JAVA_HOME/bin/java -version
else
    echo "错误: 未找到 Java 21，请先安装 Java 21"
    exit 1
fi

export PATH=$JAVA_HOME/bin:$PATH

echo ""
echo "环境设置成功! 您现在可以运行 Maven 命令了。"
