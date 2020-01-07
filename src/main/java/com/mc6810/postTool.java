package com.mc6810;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URL;

/**
 * @author rzYork
 * @group: mc6810
 * @date 2020/1/6 20:57
 * @description:
 */
public class postTool extends JFrame {
    JButton btnPost;
    JButton btnOpen;
    JScrollPane scrollPane;
    JTextArea textArea;
    JFileChooser chooser;
    JTextField urlText;
    JFrame frame;
    File file = null;
    CloseableHttpClient httpClient;
    ResponseHandler<String> responseHandler;


    public postTool(int width, int height) {
        super("postTool  byMC绿宝石 mc6810.com ");
        httpClient = HttpClients.createDefault();
        responseHandler = new BasicResponseHandler();
        frame = this;

        this.setSize(width, height);
        chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory()||f.getName().endsWith(".json");
            }

            @Override
            public String getDescription() {
                return null;
            }
        });
        btnPost = new JButton("POST");
        btnOpen = new JButton("OPEN [.JSON] FILE");
        urlText = new JTextField("PLEASE ENTER THE TERMINAL URL");
        textArea = new JTextArea(20, 50);
        scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);
        this.setLayout(null);
        this.setResizable(false);

        urlText.setBounds(30 + 100 + 180, 10, width - (30 + 100 + 180) - 10, 30);
        btnPost.setBounds(10, 10, 100, 30);
        btnOpen.setBounds(120, 10, 180, 30);
        scrollPane.setBounds(10, 50, width - 40, height - 40 - 50);

        btnOpen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = chooser.showOpenDialog(frame);
                if (i == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    try {
                        String encoding = codeString(file.getAbsolutePath());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            textArea.append(line);
                            textArea.append("\n\r");
                        }
                        reader.close();
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        btnPost.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (file == null) {
                    JOptionPane.showMessageDialog(frame, "Please open a json file first!");
                    return;
                }
                String url = urlText.getText();
                try {
                    URL u = new URL(url);
                    u.openStream();
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(frame, "Can't open the url! Please try restart the server!");
                    return;
                }
                String text = textArea.getText();

                JSONObject object = JSONObject.parseObject(text);
                try {
                    String returnValue = postJson(object, url, codeString(file.getAbsolutePath()));
                    JOptionPane.showMessageDialog(frame,"Posted !");
                    JOptionPane.showMessageDialog(frame,returnValue);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        this.add(btnOpen);
        this.add(btnPost);
        this.add(scrollPane);
        this.add(urlText);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);


    }

    public static void main(String[] args) {
        new postTool(880, 660);

    }

    public static String codeString(String fileName) throws Exception {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
        int p = (bin.read() << 8) + bin.read();
        bin.close();
        String code = null;

        switch (p) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;
            default:
                code = "GBK";
        }

        return code;
    }

    public String postJson(JSONObject jsonObject, String url, String encoding) {
        String jsonStr = jsonObject.toString();
        try {
            HttpPost httpPost = new HttpPost(url);
            StringEntity requestEntity = new StringEntity(jsonStr, encoding);
            requestEntity.setContentEncoding(encoding);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(requestEntity);
            return httpClient.execute(httpPost, responseHandler);
        } catch (Exception e) {
        }
        return null;
    }

}
