import cv2
import numpy as np

#red =0, green=177, blue=64
def make_mask(img):
    mask=np.ones((img.shape[0],img.shape[1]))
    #print(img[536,338,:])
    for i in range(img.shape[0]):
        for j in range(img.shape[1]):
            if img[i,j,0]==64 and img[i,j,1]==177 and img[i,j,2]==0:
                mask[i,j]=255
            else: mask[i,j]=0
    
    # cv2.imshow('Mask',mask)
    # cv2.waitKey(0)
    # cv2.destroyAllWindows()
    cv2.imwrite('mask1.jpg',mask)
    return mask

