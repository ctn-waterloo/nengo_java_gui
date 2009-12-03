import core

from javax.swing import *
from javax.swing.event import *
from java.awt import *
from java.awt.event import *


class FunctionControl(core.DataViewComponent,ComponentListener):
    def __init__(self,view,name,func):
        core.DataViewComponent.__init__(self)
        self.view=view
        self.name=name
        self.func=func
        self.resize_border=2
        self.range=1.0
        self.popup.add(JPopupMenu.Separator())        
        self.popup.add(JMenuItem('increase range',actionPerformed=self.increase_range))
        self.popup.add(JMenuItem('decrease range',actionPerformed=self.decrease_range))

        self.data=self.view.watcher.watch(name,func)


        values=self.data.get_first()
        self.sliders=[]
        self.labels=[]
        for i,v in enumerate(values):
            vv=int(v*100/self.range)
            if vv>100: vv=100
            if vv<-100: vv=-100
            slider=JSlider(JSlider.VERTICAL,-100,100,vv,stateChanged=lambda event,index=i: self.slider_moved(index))
            slider.background=Color.white
            self.add(slider)
            self.sliders.append(slider)
            label=JLabel('0.00')
            self.add(label)
            self.labels.append(label)
            slider.addMouseListener(self)  

            
        
        self.setSize(len(values)*40+20,200)    
        self.addComponentListener(self)
        self.componentResized(None)
        
    def increase_range(self,event):
        self.range*=2.0
        self.repaint()
    def decrease_range(self,event):
        self.range*=0.5
        self.repaint()
            
    
    def slider_moved(self,index):
        if self.sliders[index].valueIsAdjusting:   # if I moved it
            v=self.sliders[index].value*0.01*self.range
            self.labels[index].text='%1.2f'%v
            if self.view.paused:  # change immediately, bypassing filter
                self.data.data[-1][index]=v
                self.view.forced_origins_prev[(self.name,'origin',index)]=v
                
            self.view.forced_origins[(self.name,'origin',index)]=v
        
   
    def paintComponent(self,g):
        core.DataViewComponent.paintComponent(self,g)    
        
        
        self.active=self.view.current_tick>=self.view.timelog.tick_count-1
        
        data=self.data.get(start=self.view.current_tick,count=1)[0]
        if data is None: 
            data=self.data.get_first()
        
        for i,v in enumerate(data):
            sv=int(v*100.0/self.range)
            if sv>100: sv=100
            if sv<-100: sv=-100
            if not self.sliders[i].valueIsAdjusting:
                self.sliders[i].value=sv    
            self.labels[i].text='%1.2f'%v
            self.sliders[i].enabled=self.active
            
            
            
        self.componentResized(None)    


    def componentResized(self,e):
        w=self.width-self.resize_border*2
        dw=w/len(self.sliders)
        x=(dw-self.sliders[0].minimumSize.width)/2
        for i,slider in enumerate(self.sliders):
            slider.setSize(slider.minimumSize.width,self.height-self.resize_border*2-20)
            slider.setLocation(self.resize_border+x+i*dw,self.resize_border)
            self.labels[i].setLocation(slider.x+slider.width/2-self.labels[i].width/2,slider.y+slider.height)
            
    
    def componentHidden(self,e):
        pass
    def componentMoved(self,e):
        pass
    def componentShown(self,e):
        pass
        
    def save(self):
        info = core.DataViewComponent.save(self)
        info['range']=self.range
        return info
    
    def restore(self,d):
        core.DataViewComponent.restore(self,d)
        self.range=d.get('range',1.0)

