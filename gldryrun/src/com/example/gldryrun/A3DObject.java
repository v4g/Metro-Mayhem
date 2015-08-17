package com.example.gldryrun;

import android.opengl.Matrix;

public class A3DObject {
	
	private float vertices[][] = {		
			{-0.5f, 0.3f, 0.0f, 1.0f},   //top left
			{0.5f,0.3f, 0.0f, 1.0f},    // top right
			{-0.5f, -0.3f, 0.0f, 1.0f}, //bottom left
			{0.5f, -0.3f, 0.0f, 1.0f} 
			};  //bottom right
	
	private float color[] = {
			1.0f, 0.0f, 0.0f,	
			0.0f, 1.0f, 0.0f,	
			0.0f, 0.1f, 1.0f,	
			1.0f, 0.0f, 0.0f,	
			};	
		
	private float modelMatrix[];
	private float boundingBox[];
	private short drawOrder[]= {
			0,1,3,2
			//3,2,0
	};
	private float worldTransformedMatrix[];
	
	A3DObject(){
		modelMatrix = new float[16];
		Matrix.setIdentityM(modelMatrix, 0);
		worldTransformedMatrix = new float[16];
		Matrix.setIdentityM(modelMatrix, 0);
	
	}
	
	public float[] getWorldTransformedVertex(float[] vertex){
		float [] transformedVertexMatrix = new float[4];
		Matrix.multiplyMV(transformedVertexMatrix,0,modelMatrix,0,vertex,0);
		return transformedVertexMatrix;
	}
	
	public float[][] getWorldTransformedObject(){
		float [][] transformedObject = new float[vertices.length][4];
		for (int i = 0; i < vertices.length; i++){
			transformedObject[i] = getWorldTransformedVertex(vertices[i]);
			
		}
		return transformedObject;
	}
	
	public void rotateObject(float a, int x, int y, int z){
		Matrix.rotateM(modelMatrix, 0, a, x, y, z);
	}
	
	public void translateObject(float x, float y, float z){
		Matrix.translateM(modelMatrix, 0, x, y, z);
	}
	
	public float[] getModelMatrix(){
		return modelMatrix;
	}
	
	public float[][] getVertices(){
		return vertices;
	}
	public short[] getDrawOrder(){
		return drawOrder;
	}
	
	public float[] getColors(){
		return color;
	}
}
