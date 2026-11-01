import random, math, sys
from PIL import Image, ImageDraw, ImageFilter
W,H=1024,768
random.seed(7)
im=Image.new('RGB',(W,H))
d=ImageDraw.Draw(im)
top=(38,40,92); mid=(176,96,120); low=(250,176,110)
for y in range(H):
    t=y/H
    if t<0.55:
        u=t/0.55; c=tuple(int(top[i]*(1-u)+mid[i]*u) for i in range(3))
    else:
        u=(t-0.55)/0.45; c=tuple(int(mid[i]*(1-u)+low[i]*u) for i in range(3))
    d.line([(0,y),(W,y)],fill=c)
# stars
for _ in range(120):
    x=random.randint(0,W); y=random.randint(0,int(H*0.4)); r=random.choice([1,1,2])
    d.ellipse([x-r,y-r,x+r,y+r],fill=(255,245,220))
# moon
d.ellipse([760,90,840,170],fill=(255,244,214))
# far hills
def hills(base,amp,col,seed):
    random.seed(seed); pts=[(0,H)]
    ph=[random.random()*6 for _ in range(3)]
    for x in range(0,W+8,8):
        y=base+amp*(math.sin(x/140+ph[0])*0.6+math.sin(x/57+ph[1])*0.3+math.sin(x/23+ph[2])*0.1)
        pts.append((x,y))
    pts.append((W,H)); d.polygon(pts,fill=col)
hills(560,30,(92,70,110),1)
# castle
cx=520
col=(40,32,62)
def tower(x,w,h,roof):
    d.rectangle([x,560-h,x+w,600],fill=col)
    d.polygon([(x-6,560-h),(x+w/2,560-h-roof),(x+w+6,560-h)],fill=col)
    for i in range(2):
        wx=x+w*(0.3+0.4*i)
        d.rectangle([wx-3,560-h+30,wx+3,560-h+48],fill=(255,206,120))
tower(cx-40,80,190,90); tower(cx-170,56,120,60); tower(cx+120,56,140,70); tower(cx-250,44,80,46); tower(cx+210,44,90,50)
d.rectangle([cx-250,540,cx+254,600],fill=col)
for x in range(cx-250,cx+254,22): d.rectangle([x,528,x+11,542],fill=col)
hills(610,22,(52,40,76),2)
hills(670,18,(30,26,52),3)
im=im.filter(ImageFilter.GaussianBlur(1.1))
# paper grain
px=im.load()
random.seed(9)
for _ in range(60000):
    x=random.randrange(W); y=random.randrange(H); r,g,b=px[x,y]; k=random.randint(-9,9)
    px[x,y]=(max(0,min(255,r+k)),max(0,min(255,g+k)),max(0,min(255,b+k)))
im.save(sys.argv[1],optimize=True)
