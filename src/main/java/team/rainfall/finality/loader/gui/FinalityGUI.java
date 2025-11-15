package team.rainfall.finality.loader.gui;

import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.installer.Installer;
import team.rainfall.finality.loader.FileManager;
import team.rainfall.finality.loader.Loader;
import team.rainfall.finality.loader.util.Localization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FinalityGUI {
    public static FinalityGUI INSTANCE = null;
    private JFrame frame;
    private JTextArea logArea;
    private JButton lightStartButton;
    private JButton installButton;
    private JButton exitButton;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                FinalityLogger.outputToGUI = true;
                FinalityGUI window = new FinalityGUI();
                INSTANCE = window;
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public FinalityGUI() {
        initialize();
    }

    private void initialize() {
        // 主窗口设置
        frame = new JFrame();
        frame.setTitle("Finality Loader GUI");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        // 顶部面板 - 按钮区
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // 轻量级启动按钮
        lightStartButton = new JButton(Localization.bundle.getString("lightweight_launch"));
        lightStartButton.setPreferredSize(new Dimension(150, 40));
        lightStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(Loader::liteLaunch).start();
            }
        });
        topPanel.add(lightStartButton);

        // 安装按钮
        installButton = new JButton(Localization.bundle.getString("install"));
        installButton.setPreferredSize(new Dimension(150, 40));
        installButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(Installer::install).start();
                // 这里添加安装的实际逻辑
            }
        });
        topPanel.add(installButton);

        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        // 中部面板 - 日志区
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);

        // 底部面板 - 退出按钮
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        exitButton = new JButton(Localization.bundle.getString("exit"));
        exitButton.setPreferredSize(new Dimension(100, 30));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        bottomPanel.add(exitButton);

        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

    }

    public synchronized void logMessage(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}