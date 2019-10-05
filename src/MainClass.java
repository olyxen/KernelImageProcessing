import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;


public class MainClass extends Thread{
	
	
	public static int w,h;
	public static String imageName=null;
	public static BufferedImage imageInput = null;
	public static BufferedImage imageOutput = null; 
	public static BufferedImage imageOutput1 = null;
	public static int size;
	public static float matrix[];
	public static long startTime,endTime;
	public static int numberOfThreads = 4;
	public static kernel threads[] = new kernel[numberOfThreads];
	
	public static void main(String[] args) throws IOException{
		
		
		//READ THE MATRIX
		Scanner scan = new Scanner(System.in);
		System.out.print("Give the width of the matrix: ");
		size = scan.nextInt();
		
		matrix= new float[size*size];
		
		for(int i=0; i<size*size; i++){
				System.out.print("Give " + (i+1) +"th element: ");
				matrix[i]=scan.nextFloat();
		}
		
		boolean exit=true;
		Scanner scan1 = new Scanner(System.in);
		int picker;
		
		while(exit) {
			try {
				//INSERT THE IMAGE
				System.out.println("Give the name of your image or pick up one between the samples: imageX.jpg for X=1,2,...,12");
				scan.nextLine(); //This is needed to pick up the new line
				imageName = scan.nextLine();
			    imageInput = ImageIO.read(new File(imageName));
			    w = imageInput.getWidth(null);
			    h = imageInput.getHeight(null);
			    
			    System.out.println("Choose the model of processing.");
			    System.out.println("Sequence implementation = 0 --- Parallel implementation = 1 --- Both = 2");
			    picker = scan.nextInt();
			    
			    imageOutput = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			    imageOutput1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			    
			    if(picker==0) {
			    	imageOutput=sequenceImplementation();
			    	try{
						System.out.print("Save the image as: ");
						imageName = scan1.nextLine();
						ImageIO.write(imageOutput, "jpg", new File(imageName+"Sequence.jpg"));
					}catch(IOException e){
						System.out.println(e);
					}
			    }else if(picker==1) {
			    	imageOutput1=parallelImplementation();
			    	try{
						System.out.print("Save the image as: ");
						imageName = scan1.nextLine();
						ImageIO.write(imageOutput1, "jpg", new File(imageName+"Parallel.jpg"));
					}catch(IOException e){
						System.out.println(e);
					}
			    }else if(picker==2) {
			    	imageOutput=sequenceImplementation();
			    	imageOutput1=parallelImplementation();
			    	try{
						System.out.print("Save the image as: ");
						imageName = scan1.nextLine();
						ImageIO.write(imageOutput, "jpg", new File(imageName+"Sequence.jpg"));
						ImageIO.write(imageOutput1, "jpg", new File(imageName+"Parallel.jpg"));
					}catch(IOException e){
						System.out.println(e);
					}
			    }else {
			    	System.out.println("You entered different number");
			    	break;
			    }
		
			    
			} catch (IOException e) {
				System.out.println(e);
				break;
			}
			
			System.out.println("Pictures are saved");
			
			System.out.println("Do you want to filter another image with the same kernel matrix?");
			System.out.println("YES=1 --- NO=0");
			if(scan.nextInt()==0) exit=false;
		}
		
		System.out.println("Program is completed");
		
	}
	
	public static BufferedImage sequenceImplementation() {
		imageOutput = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    long startTime = System.currentTimeMillis();
	    Kernel(imageInput,imageOutput,matrix,size);
	    long endTime = System.currentTimeMillis();
	    System.out.println("Sequence Kernel processing took " + (endTime - startTime) + " milliseconds");
	    return imageOutput;
	}
	
	public static BufferedImage parallelImplementation() {
		imageOutput1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    threads[0] = new kernel(imageInput,imageOutput1,matrix,size,0,0,w/2,h/2);
	    threads[1] = new kernel(imageInput,imageOutput1,matrix,size,w/2,0,w,h/2);
	    threads[2] = new kernel(imageInput,imageOutput1,matrix,size,0,h/2,w/2,h);
	    threads[3] = new kernel(imageInput,imageOutput1,matrix,size,w/2,h/2,w,h);
	    
	    
	    startTime = System.currentTimeMillis();
	    for(int i=0; i<threads.length;i++) {
		    	threads[i].start();
	    } 
	    while(threads[0].isAlive() || threads[1].isAlive() || threads[2].isAlive() || threads[3].isAlive()) {}
		endTime = System.currentTimeMillis();
		System.out.println("Parallel Kernel processing took " + (endTime - startTime) + " milliseconds");
		return imageOutput1;
	}
	
	public static void Kernel(BufferedImage pic1,BufferedImage pic2,float matrix[],int size){
		
		int p,a,r,g,b;
		int i,j,x,y;
		int a1,r1,g1,b1;
		int k,radius; //it helps for the matrix
		
		radius = (size-1)/2;
		w = pic1.getWidth();
		h = pic1.getHeight();
		for(i=radius;i<w-radius;i++){
	    	for(j=radius;j<h-radius;j++){
	    		a1=r1=g1=b1=0;
	    		k=0;		
	    		for(x=i-radius;x<=i+radius;x++){
	    			//I need the "radius" to be able to choose the neighbors of each pixel.
    				//radius depends on the size of the matrix
		    		for(y=j-radius;y<=j+radius;y++){
		    			p = pic1.getRGB(x,y); //get the i,j pixel
		    	   		r = (p>>16) & 0xff;
		    	    	g = (p>>8) & 0xff;
		    	    	b = p & 0xff; 
		    	    	r1 += r*matrix[k];
		    	    	g1 += g*matrix[k];
		    	    	b1 += b*matrix[k];
		    	    	k++;	
		    		}
		    	}
	    		
	    		p = 0;
	    		p = (r1<<16) | (g1<<8) | b1;
	    	    pic2.setRGB(i, j, p);
	    	}
	    }
		//Sequence Filtering is done
	}

}