package com.example.gldryrun;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.*;

public class MyGLRenderer implements GLSurfaceView.Renderer{
	
	private String vertexShaderText = " \n" +
			" attribute vec4 a_position; \n" +
			" attribute vec3 attrColor; \n" +
			" attribute vec2 a_texCoords; \n" +
			" uniform mat4 u_MVMatrix; \n" +
			" varying vec2 texCoords;" +
			" varying vec3 color ; \n" +
			" void main( ) \n" +
			" { \n" +
			" gl_Position = u_MVMatrix * a_position; \n" +
			" color = attrColor ; \n" +
			" texCoords = a_texCoords;	\n" +
			" } \n" +
			" \n";
	
	private String fragmentShaderText = "" +
			" precision mediump float;" +
			" uniform sampler2D texture;" +
			" varying vec2 texCoords;" +
			" varying vec3 color; \n" +
			"void main() {" +
			" gl_FragColor =  vec4(color,1.0) ; //texture2D( texture , texCoords.st); \n" +
			"}";
	
	private float[] mvMatrix = new float[16];
	private float[] viewMatrix = new float[16];
	
	private float[] rectangleVertexData = {
			-0.5f, 0.3f,   //top left
			0.5f,0.3f,     // top right
			-0.5f, -0.3f,  //bottom left
			0.5f, -0.3f
			};  //bottom right			};
	private float[] rectangleTextureCoords = {
			0.0f, 0.0f,   //top left
			1.0f,0.0f,     // top right
			0.0f, 1.0f,  //bottom left
			1.0f, 1.0f  //bottom right
	};
	
	private short[] drawOrder = {
			0,1,2,
			1,2,3
	};
	
	private float[] colorVertexData = {
			1.0f, 0.0f, 0.0f,	
			0.0f, 1.0f, 0.0f,	
			0.0f, 0.1f, 1.0f,	
			1.0f, 0.0f, 0.0f,	
			};
	
	private Context context;
	private int 	mProgramHandle;
	private int 	positionAttrib;
	private int 	colorAttrib;
	private int 	textureCoords;
	private int 	texture;
	private int		textureHandle;
	private int 	mvMatrixHandle;
	
	private FloatBuffer positionBuffer;
	private ShortBuffer drawOrderBuffer;
	private FloatBuffer colorBuffer;
	private FloatBuffer texCordBuffer;
	private FloatBuffer mvMatrixBuffer;
	
	private A3DObject 	square;
	
	boolean doneOnce = false;
	
	public MyGLRenderer( Context context){
		this.context = context;
		
		square = new A3DObject();
		
		//create the position buffer
		positionBuffer = ByteBuffer.allocateDirect(square.getVertices().length * 4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		//load the position buffer
		for( int i = 0; i < square.getVertices().length ; i++){
			positionBuffer.put(square.getVertices()[i]);
		}
		
		colorBuffer = ByteBuffer.allocateDirect(square.getColors().length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		colorBuffer.put(square.getColors()).position(0);
		
		//create the draw order buffer
		drawOrderBuffer = ByteBuffer.allocateDirect(square.getDrawOrder().length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
		drawOrderBuffer.put(square.getDrawOrder()).position(0);
		
		//not needed
		texCordBuffer =  ByteBuffer.allocateDirect(rectangleTextureCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		texCordBuffer.put(rectangleTextureCoords).position(0);
		
		//Create the model view matrix buffer
		mvMatrixBuffer =  ByteBuffer.allocateDirect(mvMatrix.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		Matrix.setIdentityM(mvMatrix, 0);
		
		Matrix.setLookAtM(viewMatrix, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f);
		
	}
	
	private int loadTexture(int resourceId){
		int texture[] = new int[1];
		
		GLES20.glGenTextures(1, texture, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
		
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resourceId);
		
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bmp,0);
		
		bmp.recycle();
		
		return texture[0];
	}
	
	public int loadShader (String shaderSrc , int type) throws RuntimeException{
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderSrc);
		GLES20.glCompileShader(shader);
		
		int [] compileStatus = new int[1];
		
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus,0);
		if ( compileStatus[0] == 0 ){
			String log = GLES20.glGetShaderInfoLog(shader);
			throw new RuntimeException(log);
		}
		return shader;
	}
	
	public void onSurfaceDestroyed()
	{
		
	}

	@Override
	public void onDrawFrame(GL10 arg0) {
		// TODO Auto-generated method stub
		//GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
		//GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
		GLES20.glUniform1i(textureHandle,0);
		
		float time = (System.currentTimeMillis() % 3600 ) / 10;
		float scaleFactor = time/36 ;
		
		if(time > 180)
			scaleFactor = -0.01f;
		else
			scaleFactor = 0.01f;
		Matrix.setIdentityM(mvMatrix, 0);
		Matrix.rotateM(mvMatrix, 0, time, 1.0f, 1.0f, 2.0f);
		Matrix.scaleM(mvMatrix, 0, scaleFactor, scaleFactor, 1.0f);
		Matrix.translateM(mvMatrix, 0, scaleFactor, 0, 0);
		square.translateObject(scaleFactor, 0, 0);
		Matrix.multiplyMM(mvMatrix, 0,viewMatrix, 0, square.getModelMatrix() , 0);
		doneOnce = true;
		
		mvMatrixBuffer.put(mvMatrix).position(0);
		GLES20.glUniformMatrix4fv(mvMatrixHandle, 1,false, mvMatrixBuffer);
		
		
		positionBuffer.position(0);
		GLES20.glVertexAttribPointer(positionAttrib, 4, GLES20.GL_FLOAT, false, 4*4, positionBuffer);
		GLES20.glEnableVertexAttribArray(positionAttrib);

		colorBuffer.position(0);
		GLES20.glVertexAttribPointer(colorAttrib, 4, GLES20.GL_FLOAT, false, 4*3, colorBuffer);
		GLES20.glEnableVertexAttribArray(colorAttrib);
		
		texCordBuffer.position(0);
		GLES20.glVertexAttribPointer(textureCoords,4, GLES20.GL_FLOAT, false, 4*2, texCordBuffer);
		GLES20.glEnableVertexAttribArray(textureCoords);
		
		GLES20.glDrawElements(GLES20.GL_LINE_LOOP, square.getDrawOrder().length, GLES20.GL_UNSIGNED_SHORT, 0);//(GLES20.GL_LINE_STRIP, 0, 4);
		//GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 3);
		
		int errorCode = GLES20.glGetError();
		if(errorCode != 0){
			System.out.println("Error" + errorCode);
		}

	}

	@Override
	public void onSurfaceChanged(GL10 arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		GLES20.glViewport(0, 0, arg1, arg2);
		
	}

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		// TODO Auto-generated method stub
		mProgramHandle = GLES20.glCreateProgram();
		
		int vertexShader = loadShader(vertexShaderText,GLES20.GL_VERTEX_SHADER);
		
		int fragmentShader = loadShader(fragmentShaderText, GLES20.GL_FRAGMENT_SHADER);
		
		GLES20.glAttachShader(mProgramHandle, vertexShader);
		GLES20.glAttachShader(mProgramHandle, fragmentShader);
		
		//GLES20.glBindAttribLocation(mProgramHandle, 6, "a_position");
		//GLES20.glBindAttribLocation(mProgramHandle, 1, "attrColor");
		
		GLES20.glLinkProgram(mProgramHandle);
		GLES20.glUseProgram(mProgramHandle);
		
		if (GLES20.glGetError() != 0)
			{
			System.out.println("STOP HERE");
			}
		
		colorAttrib = GLES20.glGetAttribLocation(mProgramHandle, "attrColor");
		positionAttrib = GLES20.glGetAttribLocation(mProgramHandle, "a_position");
		textureCoords = GLES20.glGetAttribLocation(mProgramHandle, "a_texCoords");
		textureHandle = GLES20.glGetUniformLocation(mProgramHandle, "texture");
		mvMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
		
		int [] drawOrderBuf = new int[1];
		GLES20.glGenBuffers(1, drawOrderBuf,0);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, drawOrderBuf[0]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, drawOrderBuffer.capacity() * 2, drawOrderBuffer, GLES20.GL_STATIC_DRAW);
		
		int errorCode = GLES20.glGetError();
		if(errorCode != 0){
			System.out.println("HALT");
		}
		
		texture = loadTexture(R.drawable.ic_launcher);
	}

}
