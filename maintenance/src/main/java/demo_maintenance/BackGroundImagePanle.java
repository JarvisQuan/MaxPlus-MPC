package demo_maintenance;

import javax.swing.*;
import java.awt.*;

public class BackGroundImagePanle extends JPanel {
	private ImageIcon icon;
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}
	private int x = 0;
	private int y = 0;
	public BackGroundImagePanle(String url) {
		// TODO Auto-generated constructor stub
		icon = new ImageIcon(this.getClass().getResource(url));
	}
	public void paintComponent(Graphics g) {
		g.drawImage(icon.getImage(), x, y, getSize().width,getSize().height, this);// 图片会自动缩放
	}
}
