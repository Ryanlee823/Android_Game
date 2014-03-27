package com.bn.game.chap11.ex8;

import java.io.IOException;
import java.io.InputStream;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;

public class MyGLView extends GLSurfaceView{
	private SceneRenderer renderer;//场景渲染器
	public MyGLView(Context context) {
		super(context);
		renderer=new SceneRenderer();//创建渲染器
		this.setRenderer(renderer);//设置渲染器
		this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染   
	}
	private class SceneRenderer implements GLSurfaceView.Renderer {
		Cube cube;
		TextureRect desertRect;//沙漠矩形
		private int[] textureIds;
		private int desertTexId;
		public SceneRenderer(){}
		@Override
		public void onDrawFrame(GL10 gl) {
        	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);//清除颜色与深度缓存
            gl.glMatrixMode(GL10.GL_MODELVIEW);//设置当前矩阵为模式矩阵
            gl.glLoadIdentity();//设置当前矩阵为单位矩阵     
            gl.glTranslatef(0, 0f, -6f);  
            //中间的立方体
            gl.glPushMatrix();
            gl.glTranslatef(-1.5f, cube.b/2, 0.5f);
            cube.drawSelf(gl);
            gl.glPopMatrix();
            //远处的立方体
            gl.glPushMatrix();
            gl.glTranslatef(1.5f, cube.b/2, -5.5f);
            cube.drawSelf(gl);
            gl.glPopMatrix();
            //近处的立方体
            gl.glPushMatrix();
            gl.glTranslatef(0.5f, cube.b/2, 5.5f);
            cube.drawSelf(gl);
            gl.glPopMatrix();
            //沙漠
            gl.glPushMatrix();
            gl.glRotatef(-90, 1, 0, 0);
            desertRect.drawSelf(gl);//绘制沙漠
            gl.glPopMatrix();
            
		}
		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {           
        	gl.glViewport(0, 0, width, height); //设置视窗大小及位置         	
            gl.glMatrixMode(GL10.GL_PROJECTION);//设置当前矩阵为投影矩阵            
            gl.glLoadIdentity();//设置当前矩阵为单位矩阵            
            float ratio = (float) width / height;//计算透视投影的比例            
            gl.glFrustumf(-ratio, ratio, -1, 1, 2.5f, 100);//调用此方法计算产生透视投影矩阵
            
            GLU.gluLookAt(//设置camera位置
            		gl, 
            		0f,   //人眼位置的X
            		4f, 	//人眼位置的Y
            		5f,   //人眼位置的Z，将值调成-25f,会绕到场景后面
            		0, 	//人眼球看的点X
            		0f,   //人眼球看的点Y
            		-4,   //人眼球看的点Z
            		0, 
            		1, 
            		0
            );
		}
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {            
        	gl.glDisable(GL10.GL_DITHER);//关闭抗抖动         	
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,GL10.GL_FASTEST);//设置特定Hint项目的模式          
            gl.glClearColor(0,0,0,0); //设置屏幕背景色黑色RGBA    
            gl.glEnable(GL10.GL_CULL_FACE);//设置为打开背面剪裁
            gl.glEnable(GL10.GL_DEPTH_TEST); //启用深度测试       
            //初始化纹理
            textureIds=new int[]{
            	initTexture(gl,R.drawable.beauty0),
            	initTexture(gl,R.drawable.beauty1),
            	initTexture(gl,R.drawable.beauty2),
            	initTexture(gl,R.drawable.beauty3),
            	initTexture(gl,R.drawable.beauty4),
            	initTexture(gl,R.drawable.beauty5)
            };
            desertTexId=initTexture(gl,R.drawable.desert);
            cube=new Cube(1.4f,new float[]{1,1.2f,1.4f},textureIds);
            desertRect=new TextureRect(6,1.5f,2.4f,desertTexId,4,5);
            
            gl.glEnable(GL10.GL_FOG);//允许雾
            initFog(gl);//初始化雾
		}		
	}//SceneRenderer
	//初始化纹理
	public int initTexture(GL10 gl,int drawableId){
		//生成纹理ID
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);//生成一个纹理id放在textures数组中的0位置
		int currTextureId=textures[0];   //获取生成的纹理id 
		gl.glBindTexture(GL10.GL_TEXTURE_2D, currTextureId);//绑定该纹理id，后面的操作都是针对该id
		//设置MIN_FILTER与MAG_FILTER为MIPMAP纹理过滤方式
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR_MIPMAP_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR_MIPMAP_LINEAR);
		//生成MIPMAP纹理
		((GL11)gl).glTexParameterf(GL10.GL_TEXTURE_2D,GL11.GL_GENERATE_MIPMAP, GL10.GL_TRUE);
		//设置纹理拉伸方式为REPEAT 
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_REPEAT);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_REPEAT);
        InputStream is = this.getResources().openRawResource(drawableId);//获取图片资源的输入流
        Bitmap bitmapTmp; 
        try{
        	bitmapTmp = BitmapFactory.decodeStream(is);//通过输入流生成位图
        } 
        finally{
            try {
                is.close();//关闭流
            }catch(IOException e){
                e.printStackTrace();//打印异常
            }
        }
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmapTmp, 0);//自动设置图片的格式和类型
        bitmapTmp.recycle(); //回收图片资源
        return currTextureId;
	}
	//初始化雾
	public void initFog(GL10 gl)
	{
		float[] fogColor={1,0.91765f,0.66667f,0};//雾的颜色
		gl.glFogfv(GL10.GL_FOG_COLOR, fogColor, 0);//设置雾的颜色
		gl.glFogx(GL10.GL_FOG_MODE, GL10.GL_EXP2);//设置雾的模式
		gl.glFogf(GL10.GL_FOG_DENSITY, 0.13f);//设置雾的浓度
		gl.glFogf(GL10.GL_FOG_START, 0.5f);//设置雾的开始距离
		gl.glFogf(GL10.GL_FOG_END, 100.0f);//设置雾的结束距离
	}
}
