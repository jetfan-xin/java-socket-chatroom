package dev.jingfan.jchat.common.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/** Creates deterministic avatar icons without depending on missing binary assets. */
public final class AvatarIcons {
    private static final Color[] COLORS = {
        new Color(0x2563EB), new Color(0x7C3AED), new Color(0xDB2777),
        new Color(0xDC2626), new Color(0xEA580C), new Color(0xCA8A04),
        new Color(0x16A34A), new Color(0x0D9488), new Color(0x0891B2),
        new Color(0x4F46E5), new Color(0x9333EA), new Color(0xC026D3)
    };

    private AvatarIcons() {
    }

    public static ImageIcon create(int avatarIndex) {
        return create(avatarIndex, 40);
    }

    public static ImageIcon create(int avatarIndex, int size) {
        int normalized = Math.floorMod(avatarIndex, COLORS.length);
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(COLORS[normalized]);
            graphics.fillOval(1, 1, size - 2, size - 2);
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(12, size / 2)));
            String label = Integer.toString(normalized + 1);
            int x = (size - graphics.getFontMetrics().stringWidth(label)) / 2;
            int y = (size + graphics.getFontMetrics().getAscent()
                    - graphics.getFontMetrics().getDescent()) / 2;
            graphics.drawString(label, x, y);
        } finally {
            graphics.dispose();
        }
        return new ImageIcon(image);
    }
}
