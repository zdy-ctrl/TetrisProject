package com.sxt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

/**
 * @author 18124
 */
@SuppressWarnings("AlibabaCommentsMustBeJavadocFormat")
public class Tetris extends JFrame implements KeyListener {
    /**
     * 游戏的行数26，列数12
     */
    private static final int GAME_X = 26;
    private static final int GAME_Y = 12;
    /**
     * 文本域数组
     */
    JTextArea[][] text;
    //二位数组 1代表方块 0代表空白区域
    int[][] data;
    //显示游戏状态
    JLabel label1;
    //显示游戏分数的标签
    JLabel label2;
    //用于判断游戏是否结束
    boolean isRunning;
    //用于存储所有的方块的数组
    int[] allRect;
    //用于存储当前方块的变量
    int rect;
    //线程的休眠时间
    int time = 1000;
    //标志方块的坐标
    int x, y;
    //计算得分
    int score = 0;
    //定义一个标志变量，用于判断游戏是否暂停
    boolean gamePause = false;
    //定义一个变量用于记录按下暂停将的次数
    int pauseTimes = 0;


    /**
     * 空参构造
     */
    public Tetris() {
        //调用初始化方法
        //初始化参数
        text = new JTextArea[GAME_X][GAME_Y];
        data = new int[GAME_X][GAME_Y];
        //初始化表示游戏状态的标签
        label1 = new JLabel("游戏状态：正在游戏中！");
        //初始化表示游戏分数的标签
        label2 = new JLabel("游戏得分为：0");
        //初始化开始游戏的标志
        isRunning = true;
        //初始化存放方块的数组(16进制的数)
        allRect = new int[]{0x00cc, 0x8888, 0x000f, 0x888f, 0xf888, 0xf111, 0x111f, 0x0eee, 0xffff, 0x0008, 0x0888, 0x000e, 0x0088, 0x000c, 0x08c8, 0x00e4, 0x04c4, 0x004e, 0x08c4, 0x006c, 0x04c8, 0x00c6};
        initGamePanel();
        initExplainPanel();
        initWindows();
    }

    public static void main(String[] args) {
        Tetris tetris = new Tetris();
        tetris.gameBegin();
    }


    /**
     * 初始化窗体
     */
    public void initWindows() {
        //设置窗口大小
        this.setSize(600, 850);
        //设置窗口是否可见
        this.setVisible(true);
        //设置窗口居中
        this.setLocationRelativeTo(null);
        //设置释放窗体
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置窗口大小不可变
        this.setResizable(false);
        //设置标题
        this.setTitle("俄罗斯方块");
    }


    /**
     * 初始化游戏界面
     */
    public void initGamePanel() {
        JPanel gameMain = new JPanel();
        //网格布局
        gameMain.setLayout(new GridLayout(GAME_X, GAME_Y, 1, 1));
        //初始化面板
        for (int i = 0; i < text.length; i++) {
            for (int j = 0; j < text[i].length; j++) {
                //设置文本域的行数
                text[i][j] = new JTextArea(GAME_X, GAME_Y);
                //设置文本域的背景颜色
                text[i][j].setBackground(Color.white);
                //添加键盘监听事件
                text[i][j].addKeyListener(this);
                //初始化游戏界面
                if (j == 0 || j == text[i].length - 1 || i == text.length - 1) {
                    text[i][j].setBackground(Color.gray);
                    data[i][j] = 1;
                    //这里有方块
                }
                //设置文本区域不可编辑
                text[i][j].setEditable(false);
                //文本区域添加到主面板上
                gameMain.add(text[i][j]);
            }
        }
        //添加到窗口中
        this.setLayout(new BorderLayout());
        //添加窗口的中间位置
        this.add(gameMain, BorderLayout.CENTER);
    }


    /**
     * 初始化游戏的说明面板
     */
    public void initExplainPanel() {
        //创建游戏的左说明面板
        JPanel explainLeft = new JPanel();
        explainLeft.setLayout(new GridLayout(4, 1));
        //创建游戏的右说明面板
        JPanel explainRight = new JPanel();
        explainRight.setLayout(new GridLayout(2, 1));
        //初始化左说明面板
        //在左说明面板添加说明文字
        explainLeft.add(new JLabel("按空格键，方块变形"));
        explainLeft.add(new JLabel("按左箭头，方块左移"));
        explainLeft.add(new JLabel("按右箭头，方块右移"));
        explainLeft.add(new JLabel("按下箭头，方块下落"));
        //设置标签的内容为红色字体
        label1.setForeground(Color.RED);
        //把游戏的状态标签，游戏分数标签，添加到右说明面板
        explainRight.add(label1);
        explainRight.add(label2);
        //将左说明面板添加到窗口的左侧
        this.add(explainLeft, BorderLayout.WEST);
        //将右说明面板添加到窗口的右侧
        this.add(explainRight, BorderLayout.EAST);
    }


    /**
     * 开始游戏的方法
     */
    public void gameBegin() {
        while (true) {
            //用于判断游戏是否结束
            if (!isRunning) {
                break;
            }
            //进行游戏
            gameRun();
        }
        //在标签位置显示”游戏结束“
        label1.setText("游戏状态：游戏结束！");
    }

    /**
     * 游戏运行的方法
     */
    public void gameRun() {
        ranRect();
        //方块下落位置
        x = 0;
        y = 5;
        for (int i = 0; i < GAME_X; i++) {
            try {
                Thread.sleep(time);
                if (gamePause) {
                    i--;
                } else {
                    //判断方块是否可以下落
                    if (!canFall(x, y)) {
                        //将data置为1，表示有方块占用
                        changData(x, y);
                        //循环遍历4层，看是否有行可以消除
                        for (int j = x; j < x + 4; j++) {
                            int sum = 0;
                            for (int k = 1; k <= (GAME_Y - 2); k++) {
                                if (data[j][k] == 1) {
                                    sum++;
                                }
                            }
                            //判断是否有一行可以被消除
                            if (sum == (GAME_Y - 2)) {
                                //消除j这一行
                                removeRow(j);
                            }
                        }
                        //判断游戏是否失败
                        for (int j = 1; j <= (GAME_Y - 2); j++) {
                            if (data[3][j] == 1) {
                                isRunning = false;
                                break;
                            }
                        }
                        break;
                    } else {
                        //层数+1
                        x++;
                        //方块下落一行
                        fall(x, y);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    //判断方块是否可以继续下落的方法
    public boolean canFall(int m, int n) {
        //定义一个变量
        int temp = 0x8000;
        //遍历4*4方格
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if ((temp & rect) != 0) {
                    //判断该位置的下一行是否有方块
                    if (data[m + 1][n] == 1) {
                        return false;
                    }
                }
                n++;
                temp >>= 1;
            }
            m++;
            n = n - 4;
        }
        //可以下落
        return true;
    }


    //改变不可下落的方块对应的区域的值的方法
    public void changData(int m, int n) {
        //定义一个变量
        int temp = 0x8000;
        //遍历4*4方格
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if ((temp & rect) != 0) {
                    data[m][n] = 1;
                }
                n++;
                temp >>= 1;
            }
            m++;
            n = n - 4;
        }
    }


    //移除某一行的所有方块，令以上方块掉落的方法
    public void removeRow(int row) {
        int temp = 100;
        for (int i = row; i >=1; i--) {
            for (int j = 1; j <= (GAME_Y- 2); j++) {
                //进行覆盖
                data[i][j] = data[i-1][j];
            }
        }
        //刷新游戏区域
        refresh(row);
        //方块加速
        if (time > temp) {
            time -= temp;
        }
        score += temp;
        //显示变化后的分数
        label2.setText("游戏的得分：" + score);
    }
    /**
     * 随机生成下落方块形状的方法
     */
    public void ranRect() {
        Random random = new Random();
        rect = allRect[random.nextInt(22)];
    }


    //刷新移除某一行后的游戏界面的方法
    public void refresh(int row) {
        //遍历row行以上的游戏区域
        for (int i = row; i >= 1; i--) {
            for (int j = 1; j <= (GAME_Y - 2); j++) {
                if (data[i][j] == 1) {
                    text[i][j].setBackground(Color.BLACK);
                } else {
                    text[i][j].setBackground(Color.white);
                }
            }
        }
    }


    //方块下落掉落一层的方法
    public void fall(int m, int n) {
        if (m > 0) {
            //清除上一层方块
            clear(m - 1, n);
        }
        //重新绘制方块
        draw(m, n);
    }


    //清除方块掉落后，上一层有颜色的地方的方法
    public void clear(int m, int n) {
        //定义一个变量
        int temp = 0x8000;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if ((temp & rect) != 0) {
                    text[m][n].setBackground(Color.WHITE);
                }
                n++;
                temp >>= 1;
            }
            m++;
            n = n - 4;
        }
    }


    //重新绘制掉落后方块的方法
    public void draw(int m, int n) {
        int temp = 0x8000;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if ((temp & rect) != 0) {
                    text[m][n].setBackground(Color.BLACK);
                }
                n++;
                temp >>= 1;
            }
            m++;
            n = n - 4;
        }
    }



    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == 'p') {
            //判断游戏是否结束
            if (!isRunning) {
                return;
            }
            pauseTimes++;
            //判断按下一次，暂停游戏
            if (pauseTimes == 1) {
                gamePause = true;
                label1.setText("游戏状态：暂停中！");
            }
            //判断按下两次，暂停游戏
            if (pauseTimes == 2) {
                gamePause = false;
                pauseTimes = 0;
                label1.setText("游戏状态：正在进行中！");
            }
        }
        //控制方块进行变形(空格键对应的值为)
        if (e.getKeyChar() == KeyEvent.VK_SPACE) {
            //判断游戏是否结束
            if (!isRunning) {
                return;
            }
            //判断游戏是否暂停
            if (gamePause) {
                return;
            }
            //定义变量，存储目前方块的索引
            int old;
            for (old = 0; old < allRect.length; old++) {
                //判断是否是当前方块
                if (rect == allRect[old]) {
                    break;
                }
            }
            //定义一个变量，存储变形后的方块
            int next;
            //判断是方块
            if (old == 0 || old == 7 || old == 8 || old == 9) {
                return;
            }
            //清除当前方块
            clear(x, y);
            //变形的过程
            if (old == 1 || old == 2) {
                next = allRect[old == 1 ? 2 : 1];
                if (canTurn(next, x, y)) {
                    rect = next;
                }
            }
            if (old >= 3 && old <= 6) {
                next = allRect[old + 1 > 6 ? 3 : old + 1];
                if (canTurn(next, x, y)) {
                    rect = next;
                }
            }
            if (old == 10 || old == 11) {
                next = allRect[old == 10 ? 11 : 10];
                if (canTurn(next, x, y)) {
                    rect = next;
                }
            }
            if (old == 12 || old == 13) {
                next = allRect[old == 12 ? 13 : 12];
                if (canTurn(next, x, y)) {
                    rect = next;
                }
            }
            if (old >= 14 && old <= 17) {
                next = allRect[old + 1 > 17 ? 14 : old + 1];
                if (canTurn(next, x, y)) {
                    rect = next;
                }
            }
            if (old == 18 || old == 19) {
                next = allRect[old == 18 ? 19 : 18];
                if (canTurn(next, x, y)) {
                    rect = next;
                }
            }
            if (old == 20 || old == 21) {
                next = allRect[old == 20 ? 21 : 20];
                if (canTurn(next, x, y)) {
                    rect = next;
                }
            }
            //重新绘制变形后的方块
            draw(x,y);
        }
    }

    //判断方块此时是否可以变形的方法
    public boolean canTurn(int a, int m, int n) {
        //创建变量
        int temp = 0x8000;
        //遍历整个方块
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if ((a & temp) != 0) {
                    if (data[m][n] == 1) {
                        return false;
                    }
                }
                n++;
                temp >>= 1;
            }
            m++;
            n = n - 4;
        }
        //说明我们的方块可以变形
        return true;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //方块进行左移(37代表键盘的>)
        if (e.getKeyCode() == 37) {
            //判断游戏是否结束
            if (!isRunning) {
                return;
            }
            //判断游戏是否暂停
            if (gamePause) {
                return;
            }
            //方块是否碰到左墙壁
            if (y <= 1) {
                return;
            }
            //方块是否碰到其他方块
            int temp = 0x8000;
            for (int i = x; i < x + 4; i++) {
                for (int j = y; j < y + 4; j++) {
                    if ((temp & rect) != 0) {
                        if (data[i][j - 1] == 1) {
                            return;
                        }
                    }
                    temp >>= 1;
                }
            }
            //首先清除目前方块
            clear(x, y);
            y--;
            draw(x, y);
        }
        //控制方块的右移（右箭头对应的数值为39）
        if (e.getKeyCode() == 39) {
            //判断游戏是否结束
            if (!isRunning) {
                return;
            }
            //判断游戏是否暂停
            if (gamePause) {
                return;
            }
            //方块是否碰到左墙壁
            int temp = 0x8000;
            int m = x;
            int n = y;
            //存储最右边的坐标值
            int num = 1;
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if ((temp & rect) != 0) {
                        if (n > num) {
                            num = n;
                        }
                    }
                    n++;
                    temp >>= 1;
                }
                m++;
                n = n - 4;
            }
            //判断是否碰到右墙壁
            if (num >= (GAME_Y - 2)) {
                return;
            }
            //方块是否碰到其他方块
            temp = 0x8000;
            for (int i = x; i < x + 4; i++) {
                for (int j = y; j < y + 4; j++) {
                    if ((temp & rect) != 0) {
                        if (data[i][j +1] == 1) {
                            return;
                        }
                    }
                    temp >>= 1;
                }
            }
            //清除当前方块
            clear(x, y);
            y++;
            draw(x, y);
        }
        //控制方块下落（下箭头对应的数值为40）
        if (e.getKeyCode() == 40) {
            //判断游戏是否结束
            if (!isRunning) {
                return;
            }
            //判断游戏是否暂停
            if (gamePause) {
                return;
            }
            //判断方块是否可以下落
            if (!canFall(x, y)) {
                return;
            }
            clear(x, y);
            //改变方块的坐标
            x++;
            draw(x, y);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
