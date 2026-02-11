package view;

import model.entity.Player;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
// - PLAYER RENDER CLASS
//  responsible for rendering the player on the screen
//-------------------------------------------------------------------------------------------------------------------   

public class PlayerRender {

    public void draw(Graphics2D g2, Player player){
        BufferedImage frame = player.getCurrentFrame();
        
        int x = player.getX();
        int y = player.getY();
        int width = player.getSpriteWidth();
        int height = player.getSpriteHeight();

        if (player.getFacingRight() == 1) {
            // Draw normally
            g2.drawImage(frame, x, y, width,  height, null);
        } else {
            // Draw flipped
            g2.drawImage(frame, x + width, y, -width, height, null);
        }
    }
}
//-------------------------------------------------------------------------------------------------------------------
