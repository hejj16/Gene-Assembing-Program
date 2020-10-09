import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.math.BigInteger;
import java.io.*;
import java.util.*;

public class DBG {
	String file_position;//�ļ�λ��
	static String input="duplicate.fastq";
	static int quality_threshold=0;//��Ҫ���õ���ֵ
	static int k=13;//Ĭ��kֵ
	int L=0;//һ��read�ĳ���
	int[] k_mer_addr=new int[(int)1e8];
	int[] k_mer_num=new int[(int)1e8];
	int[] k_mer_cur=new int[(int)1e8];
	readID_pos readid[];
	static String outpass="";
	static String output_pos="output.txt";
	static String reference_pos="reference.txt";
	int Dangqianshuliang=0;
	static Object Contigs[][]=new Object[20][3];
	static int blast_or_not=0;
	static int if_read_finished=0;
	static int if_contig_finished=0;
	static int if_blast_finished=0;
	
	int read_num=0;//����ͳ��read������
	int used_read_num=0;//����ͳ�Ʋ���ƴ��read������
	
	public void read(String file_position) throws IOException {
		if_read_finished=0;
		//������ȡԴ�ļ�������kmer
		this.file_position=file_position;
		BufferedReader file=new BufferedReader(new InputStreamReader(new FileInputStream(file_position)));
		String Line_1;
		String Line_2;
		String Line_3;
	    String Line_4;
	    int readID_value=0;//������ʶ���ڶ����˵ڼ���read
	    
		while(true) {
			Line_1=file.readLine();
		    if(Line_1!=null&&Line_1!="") {
		    	read_num++;
		    }else {
		    	break;
		    }


		    System.out.println(4*read_num);
		    Line_2=file.readLine();
		    Line_3=file.readLine();
	     	Line_4=file.readLine();
	     	if(L==0) {
	     		L=Line_2.length();//ֻ��ֵһ��
	     	}
	     	int n=0;//�����ж�����δ֪���
		    char chr;
		    int quality_value_A=0,quality_value_num=0;
		    if(Line_2==null||Line_2=="") {
		    	break;
		    }
		    if(Line_3==null||Line_3=="") {
		    	break;
		    }
		    if(Line_4==null||Line_4=="") {
		    	break;
		    }
		    for(int i=0;i<Line_2.length();i++) {
			    chr=Line_2.charAt(i);
			    quality_value_num+=(int)Line_4.charAt(i);
			    if (chr=='A') {
				    quality_value_A++;
			    }
			    if(chr!='A'&&chr!='T'&&chr!='G'&&chr!='C') {
			    	n++;
			    }
	    	}
		    double rate_of_A=(double)quality_value_A/(double)Line_2.length();
		    if(rate_of_A<0.9&&quality_value_num>quality_threshold&&n==0) {
		    	used_read_num++;
		    	kmer(Line_2,readID_value,Line_2.length());//����k-mer
		    	readID_value++;
		    }
	    }
		file.close();
		/*
		 * ���²���Ϊ�ڶ��ζ��ļ��Ĳ���
		 */
		/*
		 * �ȱ���DBG��ΪreadID_pos���ٿռ�
		 */
		int readID_pos_size=0;
		for(int k=0;k<(int)1e8;k++) {
			if(k_mer_num[k]!=0) {
				k_mer_addr[k]=readID_pos_size;
				readID_pos_size++;	
			}
		}
		readid=new readID_pos[readID_pos_size];//Ϊreadid��ļ��Ͽ�����Ӧ��С�Ŀռ�
		for(int k=0;k<(int)1e8;k++) {
			if(k_mer_num[k]!=0) {
				readid[k_mer_addr[k]]=new readID_pos(k_mer_num[k],3);
			}
		}
		
		
		/*
		 * ������ڶ����ļ�
		 */
		BufferedReader file_2=new BufferedReader(new InputStreamReader(new FileInputStream(file_position)));
		String Line_1_2;
		String Line_2_2;
		String Line_3_2;
	    String Line_4_2;
	    int readidvalue=0;
		while(true) {
		    try{
		    	Line_1_2=file_2.readLine();
		    	readidvalue++;
		    	if(Line_1_2==null||Line_1_2=="") {
		    		break;
		    	}
		    	}
		    catch(Exception e){
		    	break;
		    	}
		    Line_2_2=file_2.readLine();
		    Line_3_2=file_2.readLine();
	     	Line_4_2=file_2.readLine();
	     	int n=0;//�����ж�����δ֪���
		    char chr;
		    int quality_value_A=0,quality_value_num=0;
		    if(Line_2_2==null||Line_2_2=="")break;
		    if(Line_3_2==null||Line_3_2=="")break;
		    if(Line_4_2==null||Line_4_2=="")break;
		    for(int i=0;i<Line_2_2.length();i++) {
			    chr=Line_2_2.charAt(i);
			    quality_value_num+=(int)Line_4_2.charAt(i);
			    if (chr=='A') {
				    quality_value_A++;
			    }
			    if(chr=='N') {
			    	n++;
			    }
	    	}
		    double rate_of_A=(double)quality_value_A/(double)Line_2_2.length();
		    if(rate_of_A<0.9&&quality_value_num>quality_threshold&&n==0) {
		    	kmer_2(Line_2_2,readidvalue,Line_2_2.length());
		    }
	    }
		file_2.close();
	
		
	
		
		if_read_finished=1;
	}
	
	public void kmer(String Line_2,int readID_value,int L) {
		//������������read��ֳ�kmer
		int kmer_num=L-k+1;//һ��read����������ô��kmer
		for(int i=0;i<kmer_num;i++) {
			String kmer=Line_2.substring(i, i+k);
			String kmer_hashvalue="";
			for(int j=0;j<kmer.length();j++) {
				char base=kmer.charAt(j);
				String base_value=null;
				if(base=='A') {
					base_value="00";
				}
				if(base=='C') {
					base_value="01";
				}
				if(base=='G') {
					base_value="10";
				}
				if(base=='T') {
					base_value="11";
				}
				StringBuffer a=new StringBuffer(kmer_hashvalue);
				a.append(base_value);
				kmer_hashvalue=a.toString();
			}
			int kmer_seq=Integer.valueOf(kmer_hashvalue,2);
			k_mer_num[kmer_seq]++;
		}
	}
	
	public void kmer_2(String Line_2,int readidvalue,int L) {
		//������������read��ֳ�kmer
		int kmer_num=L-k+1;//һ��read����������ô��kmer
		for(int i=0;i<kmer_num;i++) {
			String kmer=Line_2.substring(i, i+k);
			String kmer_hashvalue="";
			for(int j=0;j<kmer.length();j++) {
				char base=kmer.charAt(j);
				String base_value=null;
				if(base=='A') {
					base_value="00";
				}
				if(base=='C') {
					base_value="01";
				}
				if(base=='G') {
					base_value="10";
				}
				if(base=='T') {
					base_value="11";
				}
				StringBuffer a=new StringBuffer(kmer_hashvalue);
				a.append(base_value);
				kmer_hashvalue=a.toString();
			}
			int kmer_seq=Integer.valueOf(kmer_hashvalue,2);
			readid[k_mer_addr[kmer_seq]].a[k_mer_cur[kmer_seq]][2]=1;//1��ʾδɾ����-1��ʾɾ��
			readid[k_mer_addr[kmer_seq]].a[k_mer_cur[kmer_seq]][0]=readidvalue;
			readid[k_mer_addr[kmer_seq]].a[k_mer_cur[kmer_seq]][1]=i;
			k_mer_cur[kmer_seq]++;
			
		}
	}
	
	public String _10_2(int n){//ʮ����ת��������

		  /*
	      int t = 0;  //������¼λ��
	      int bin = 0; //������¼���Ķ�������
	      int r = 0;  //�����洢����
	      while(n != 0){
	          r = n % 2;
	          n = n / 2;
	          bin += r * Math.pow(10,t);
	          t++; 
	     }
	     */
		    BigInteger bi = new BigInteger(String.valueOf(n));	//ת����BigInteger����
	    	String bin=bi.toString(2);
	    	String i=bin;
			if(i.length()!=2*k) {
				int leng=i.length();
			    for(int o=0;o<2*k-leng;o++) {
			    	StringBuffer _0=new StringBuffer(i);
				    _0.insert(0, "0");
				    i=_0.toString();
			    }
			}
	        return i;
	 }
	public String reverse(int ii) {
		//�ú�����������kmer������kmer����ʮ���ƹ�ϣֵ���������kmer�Ķ������ַ���ֵ
		String i=_10_2(ii);
		if(i.length()!=2*k) {
			int leng=i.length();
		    for(int o=0;o<2*k-leng;o++) {
		    	StringBuffer _0=new StringBuffer(i);
			    _0.insert(0, "0");
			    i=_0.toString();
		    }
		}
		int LENG=i.length();
		for(int a=0;a<LENG;a+=2) {
			String _i=i.substring(a,a+2);
			if(_i.equals("00")){
				StringBuffer add=new StringBuffer(i);
				add.append("11");
				add.toString();
			}
			if(_i.equals("11")){
				StringBuffer add=new StringBuffer(i);
				add.append("00");
				add.toString();
			}
			if(_i.equals("01")){
				StringBuffer add=new StringBuffer(i);
				add.append("01");
				add.toString();
			}
			if(_i.equals("10")){
				StringBuffer add=new StringBuffer(i);
				add.append("10");
				add.toString();
			}
		}

		return new StringBuffer(i).reverse().toString();
	}
	
	//�����Ǿ��߱���ش�����
	
	
	
	
	
	
	ArrayList<int[]> Dec_table=new ArrayList<int[]>();//���߱�Ľ���
	static int min_read_num=25;//������ֵ�������ֶ�����
	int cur_kmer=-1,re_cur_kmer,last_kmer,re_last_kmer;
	int cur_pos=0;
	int locked_read_num=0;//��¼����������
	int MIN_LOCKED_NUM=1000;//�������õ���ֵ,���������������Ƚ�
	int stopcode=0;//�ж�һ���ص�Ⱥ�Ƿ�ƴ����ϣ�1����ϣ�0��û��
	static int contig_value=0;
	int end=0;//�����ж���ȫ����
	String contig;
	ArrayList<Integer> shuliang=new ArrayList<Integer>();
	ArrayList<Integer> shuliang_2=new ArrayList<Integer>();
	
	public void Contig() throws IOException {

		int Origin_kmer=0;
		cur_kmer=0;
		contig="";
		Dec_table.clear();
		stopcode=0;
		int judge=0;
		shuliang.clear();
		shuliang_2.clear();

		
		for(int i=0;i<(int)1e8;i++) {
			if(k_mer_num[i]>min_read_num) {
				int cout_read_num=0;
				for(int j=0;j<k_mer_num[i];j++) {
					if(readid[k_mer_addr[i]].a[j][1]==0&&readid[k_mer_addr[i]].a[j][2]==1) {
						cout_read_num++;
					}
				}
				if(cout_read_num>min_read_num) {
					//if(cur_kmer==-1) {
						cur_kmer=i;
						judge=1;
						Origin_kmer=i;


					    for(int j=0;j<k_mer_num[cur_kmer];j++) {
						    if(readid[k_mer_addr[cur_kmer]].a[j][1]==0&&readid[k_mer_addr[i]].a[j][2]==1) {
							    int[] DEC=new int[9];
							    DEC[0]=readid[k_mer_addr[cur_kmer]].a[j][0];
							    DEC[1]=1;
							    DEC[2]=0;
							    DEC[3]=0;
							    DEC[4]=readid[k_mer_addr[cur_kmer]].a[j][1];
							    DEC[5]=1;
							    DEC[8]=0;
							    Dec_table.add(DEC);
							    readid[k_mer_addr[cur_kmer]].a[j][2]=0;//0����ƴ��
							    Dangqianshuliang++;
						    }
					    }
					    
				    }
				//}
			}
			if(judge==1) {
				break;
			}
		}
		if(judge==0) {
			end=1;
		}
////////////////////////////////////////////////////////
/*		
cur_kmer=Integer.valueOf("001010101010101010101010",2);


for(int j=0;j<k_mer_num[cur_kmer];j++) {
if(readid[k_mer_addr[cur_kmer]].a[j][1]==0) {
int[] DEC=new int[9];
DEC[0]=readid[k_mer_addr[cur_kmer]].a[j][0];
DEC[1]=1;
DEC[2]=0;
DEC[3]=0;
DEC[4]=readid[k_mer_addr[cur_kmer]].a[j][1];
DEC[5]=1;
DEC[8]=0;
Dec_table.add(DEC);
}
}
*//////////////////////////////////////////////////////////
		//����Ҫ�����kmer�ټ�����߱�
		
		re_cur_kmer=Integer.valueOf(reverse(cur_kmer),2);
		for(int i=0;i<k_mer_num[re_cur_kmer];i++) {
			if(readid[k_mer_addr[re_cur_kmer]].a[i][1]>L-k+1-3&&readid[k_mer_addr[re_cur_kmer]].a[i][2]==1) {
				int DEC[]=new int[9];
				DEC[0]=readid[k_mer_addr[re_cur_kmer]].a[i][0];
				DEC[1]=-1;
				DEC[2]=readid[k_mer_addr[re_cur_kmer]].a[i][1];
				DEC[3]=readid[k_mer_addr[re_cur_kmer]].a[i][1];
				DEC[4]=readid[k_mer_addr[re_cur_kmer]].a[i][1];
				DEC[5]=1;DEC[8]=0;
				Dec_table.add(DEC);
				readid[k_mer_addr[re_cur_kmer]].a[i][2]=0;
				Dangqianshuliang++;
			}
		}
		

		contig=num_to_ATGC(_10_2(cur_kmer));
		for(int u=0;u<k;u++) {
			shuliang.add(Dangqianshuliang);
		}
		Dangqianshuliang=0;
		


		while(stopcode==0&&end==0) {

			refresh_1();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println(num_to_ATGC(_10_2(cur_kmer)));
			for(int a=0;a<Dec_table.size();a++) {
				if(Dec_table.get(a)[8]!=-100) {
				for(int b=0;b<9;b++) {
					System.out.print(Dec_table.get(a)[b]);
					System.out.print(" ");
				}
				System.out.println("");
				}
			}
			System.out.println(contig);
			System.out.println("");
			
			

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(stopcode==0) {
				{
					StringBuffer x=new StringBuffer(contig);
					x.append(num_to_ATGC(_10_2(cur_kmer)).substring(k-1, k));
					contig=x.toString();
					
					shuliang.add(Dangqianshuliang);
					
				}
				refresh_2();
				
			}
		}
		

	//////////////////////////////////////////////////	

		
///////////////////////////////////////////////////////////		
		
	////���淴�����죡������������������������������������
		contig=new StringBuffer(contig).reverse().toString();
	
		
		stopcode=0;
		/*
		String __cur_kmer="";
		String _cur_kmer=new StringBuffer(num_to_ATGC(_10_2(Origin_kmer))).reverse().toString();
		for(int j=0;j<_cur_kmer.length();j++) {
			char base=_cur_kmer.charAt(j);
			String base_value=null;
			if(base=='A') {
				base_value="00";
			}
			if(base=='C') {
				base_value="01";
			}
			if(base=='G') {
				base_value="10";
			}
			if(base=='T') {
				base_value="11";
			}
			StringBuffer a=new StringBuffer(__cur_kmer);
			a.append(base_value);
			__cur_kmer=a.toString();
			re_cur_kmer=Integer.valueOf(__cur_kmer,2);
		}
		
		cur_kmer=Integer.valueOf(reverse(re_cur_kmer),2);
		*/
		re_cur_kmer=Origin_kmer;
		cur_kmer=Integer.valueOf(reverse(re_cur_kmer),2);
		
		
		
		
		for(int i=0;i<k_mer_num[cur_kmer];i++) {
			if(readid[k_mer_addr[cur_kmer]].a[i][1]==0&&readid[k_mer_addr[cur_kmer]].a[i][2]==1) {
				int DEC[]=new int[9];
				DEC[0]=readid[k_mer_addr[cur_kmer]].a[i][0];
				DEC[1]=1;
				DEC[2]=readid[k_mer_addr[cur_kmer]].a[i][1];
				DEC[3]=readid[k_mer_addr[cur_kmer]].a[i][1];
				DEC[4]=readid[k_mer_addr[cur_kmer]].a[i][1];
				DEC[5]=1;DEC[8]=0;
				Dec_table.add(DEC);
				readid[k_mer_addr[cur_kmer]].a[i][2]=0;
			}
		}
					    

		//����Ҫ�����kmer�ټ�����߱�

		for(int i=0;i<k_mer_num[re_cur_kmer];i++) {
			if(readid[k_mer_addr[re_cur_kmer]].a[i][1]>L-k+1-3&&readid[k_mer_addr[re_cur_kmer]].a[i][2]==1) {
				int DEC[]=new int[9];
				DEC[0]=readid[k_mer_addr[re_cur_kmer]].a[i][0];
				DEC[1]=-1;
				DEC[2]=readid[k_mer_addr[re_cur_kmer]].a[i][1];
				DEC[3]=readid[k_mer_addr[re_cur_kmer]].a[i][1];
				DEC[4]=readid[k_mer_addr[re_cur_kmer]].a[i][1];
				DEC[5]=1;DEC[8]=0;
				Dec_table.add(DEC);
				readid[k_mer_addr[re_cur_kmer]].a[i][2]=0;
			}
		}
		
		while(stopcode==0&&end==0) {

			refresh_1();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			for(int a=0;a<Dec_table.size();a++) {
				if(Dec_table.get(a)[8]!=-100) {
				for(int b=0;b<9;b++) {
					System.out.print(Dec_table.get(a)[b]);
					System.out.print(" ");
				}
				System.out.println("");
				}
			}
			System.out.println(contig);
			System.out.println("");
			

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(stopcode==0) {
				{
					StringBuffer x=new StringBuffer(contig);
					x.append(num_to_ATGC(_10_2(re_cur_kmer)).substring(k-1, k));
					contig=x.toString();
					
					shuliang.add(0,Dangqianshuliang);

				}
				refresh_2();
				
			}
		}
		
		
		
		for(int i=0;i<shuliang.size();i++) {
			shuliang_2.add(shuliang.get(i)/2+1);
		}
		contig=new StringBuffer(contig).reverse().toString();
		
		int max=0;
		for(int m=0;m<shuliang_2.size();m++) {
			if(max<shuliang_2.get(m)) max=shuliang_2.get(m);
		}
		
		FileWriter w=new FileWriter(outpass+"Graph"+contig_value+output_pos);
		for(int m=0;m<max;m++) {
			
			String graph="";
			for(int n=0;n<shuliang_2.size();n++) {
				if(shuliang_2.get(n)>=max-m) {
					StringBuffer x=new StringBuffer(graph);
					x.append("������������");
					graph=x.toString();
				}else {
					StringBuffer x=new StringBuffer(graph);
					x.append("------");
					graph=x.toString();
				}
			}
			w.write(graph);
			w.write("\r\n");
		}

		w.close();
		
		FileWriter ww=new FileWriter(outpass+contig_value+output_pos);
		ww.write(contig);

		ww.close();


		
		
		
	}
	public void refresh_1() {
		
		//�����Ĳ������ܵĽ��,Ȼ�����������ҵ�read��ͬ��СƬ�Σ����������
		String cur_kmer_str=_10_2(cur_kmer);
		last_kmer=cur_kmer;
		re_last_kmer=re_cur_kmer;
		cur_kmer=find_next(cur_kmer_str);
		boolean cur=true,last=true;
		if(cur_kmer==-1) {
			stopcode=1;
		}
		if(cur_kmer!=-1) {
			re_cur_kmer=Integer.valueOf(reverse(cur_kmer),2);
			for(int i=0;i<Dec_table.size();i++) {
				if(Dec_table.get(i)[1]==1&&Dec_table.get(i)[8]!=100&&Dec_table.get(i)[8]!=-100) {//��������kmer
					for(int j=0;j<k_mer_num[cur_kmer];j++) {
						if(Dec_table.get(i)[0]==readid[k_mer_addr[cur_kmer]].a[j][0]
								&&Dec_table.get(i)[3]+1==readid[k_mer_addr[cur_kmer]].a[j][1]
										&&readid[k_mer_addr[cur_kmer]].a[j][2]!=-1) {//Ҫ��Ҫ��ɾ����ǵ��ж���???????
							cur=true;
							break;
						}else {
							cur=false;
						}
					}
					for(int j=0;j<k_mer_num[last_kmer];j++) {
						if(Dec_table.get(i)[0]==readid[k_mer_addr[last_kmer]].a[j][0]
								&&Dec_table.get(i)[3]==readid[k_mer_addr[last_kmer]].a[j][1]
										&&readid[k_mer_addr[last_kmer]].a[j][2]!=-1) {//Ҫ��Ҫ��ɾ����ǵ��ж���???????
							last=true;
							break;
						}else {
							last=false;
						}
					}
					if(cur&&last) {
						Dec_table.get(i)[3]+=1;
						Dec_table.get(i)[4]=Dec_table.get(i)[3];
						Dec_table.get(i)[5]+=1;
					}if(cur==true&&last==false) {
						Dec_table.get(i)[5]=Dec_table.get(i)[6]+1;
						Dec_table.get(i)[6]=0;
						Dec_table.get(i)[7]++;
						Dec_table.get(i)[3]++;
						Dec_table.get(i)[4]=Dec_table.get(i)[3];	
					}if(cur==false&&last==true) {
						Dec_table.get(i)[6]++;
						Dec_table.get(i)[3]++;//�����в�һ����3��4��һ����ʱ��˵������readû����ƴ��
					}if(!cur&&!last) {
						Dec_table.get(i)[6]++;
						Dec_table.get(i)[3]++;
					}
					if(Dec_table.get(i)[5]==L-k+1) {
						Dec_table.get(i)[8]=100;//100�ɹ���-100ʧ��,0δƴ�ӣ�1����ƴ��
						int kill=Dec_table.get(i)[0];
						for(int p=0;p<(int)1e8;p++) {
							if(k_mer_num[p]!=0) {
								for(int q=0;q<k_mer_num[p];q++) {
									if(readid[k_mer_addr[p]].a[q][0]==kill) {
										readid[k_mer_addr[p]].a[q][2]=-1;
									}
								}
								
							}
						}
					}else if(Dec_table.get(i)[6]>k||Dec_table.get(i)[7]>1) {
						Dec_table.get(i)[8]=-100;						
						int _kill=Dec_table.get(i)[0];
						//Dec_table.remove(i);

						for(int p=0;p<(int)1e8;p++) {
							if(k_mer_num[p]!=0) {
								for(int q=0;q<k_mer_num[p];q++) {
									if(readid[k_mer_addr[p]].a[q][0]==_kill) {
										readid[k_mer_addr[p]].a[q][2]=1;
									}
								}
								
							}
						}
					}
						

				}
				else if(Dec_table.get(i)[1]==-1&&Dec_table.get(i)[8]!=100&&Dec_table.get(i)[8]!=-100){//���·���kmer
					for(int j=0;j<k_mer_num[re_cur_kmer];j++) {
						if(Dec_table.get(i)[0]==readid[k_mer_addr[re_cur_kmer]].a[j][0]
								&&Dec_table.get(i)[3]-1==readid[k_mer_addr[re_cur_kmer]].a[j][1]
										&&readid[k_mer_addr[re_cur_kmer]].a[j][2]!=-1) {//Ҫ��Ҫ��ɾ����ǵ��ж���???????
							cur=true;
							break;
						}else {
							cur=false;
						}
					}
					for(int j=0;j<k_mer_num[re_last_kmer];j++) {
						if(Dec_table.get(i)[0]==readid[k_mer_addr[re_last_kmer]].a[j][0]
								&&Dec_table.get(i)[3]==readid[k_mer_addr[re_last_kmer]].a[j][1]
										&&readid[k_mer_addr[re_last_kmer]].a[j][2]!=-1) {//Ҫ��Ҫ��ɾ����ǵ��ж���???????
							last=true;
							break;
						}else {
							last=false;
						}
					}
					if(cur&&last) {
						Dec_table.get(i)[3]--;
						Dec_table.get(i)[4]=Dec_table.get(i)[3];
						Dec_table.get(i)[5]+=1;
					}if(cur==true&&last==false) {
						Dec_table.get(i)[5]=Dec_table.get(i)[6]+1;
						Dec_table.get(i)[6]=0;
						Dec_table.get(i)[7]++;
						Dec_table.get(i)[3]--;
						Dec_table.get(i)[4]=Dec_table.get(i)[3];	
					}if(cur==false&&last==true) {
						Dec_table.get(i)[6]++;
						Dec_table.get(i)[3]--;//�����в�һ����3��4��һ����ʱ��˵������readû����ƴ��
					}if(!cur&&!last) {
						Dec_table.get(i)[6]++;
						Dec_table.get(i)[3]--;
					}
					if(Dec_table.get(i)[5]>=L-k-1) {
						Dec_table.get(i)[8]=100;//100�ɹ���-100ʧ��
						int kill=Dec_table.get(i)[0];
						for(int p=0;p<(int)1e8;p++) {
							if(k_mer_num[p]!=0) {
								for(int q=0;q<k_mer_num[p];q++) {
									if(readid[k_mer_addr[p]].a[q][0]==kill) {
										readid[k_mer_addr[p]].a[q][2]=-1;
									}
								}
							}
						}
					}else if(Dec_table.get(i)[6]>k||Dec_table.get(i)[7]>1||(Dec_table.get(i)[7]==1&&Dec_table.get(i)[2]!=L-k+1)) {//                 
						Dec_table.get(i)[8]=-100;
						int _kill=Dec_table.get(i)[0];
						//Dec_table.remove(i);
						for(int p=0;p<(int)1e8;p++) {
							if(k_mer_num[p]!=0) {
								for(int q=0;q<k_mer_num[p];q++) {
									if(readid[k_mer_addr[p]].a[q][0]==_kill) {
										readid[k_mer_addr[p]].a[q][2]=1;
									}
								}
								
							}
						}
					}
				}
			}
		}		
	}
	
	public void refresh_2() {
		for(int i=0;i<k_mer_num[cur_kmer];i++) {
			//System.out.println(num_to_ATGC(_10_2(cur_kmer)));
			if(readid[k_mer_addr[cur_kmer]].a[i][1]==0&&readid[k_mer_addr[cur_kmer]].a[i][2]==1) {
				int DEC[]=new int[9];
				DEC[0]=readid[k_mer_addr[cur_kmer]].a[i][0];
				DEC[1]=1;
				DEC[2]=readid[k_mer_addr[cur_kmer]].a[i][1];
				DEC[3]=readid[k_mer_addr[cur_kmer]].a[i][1];
				DEC[4]=readid[k_mer_addr[cur_kmer]].a[i][1];
				DEC[5]=1;
				Dec_table.add(DEC);
				readid[k_mer_addr[cur_kmer]].a[i][2]=0;
			}
		}
		for(int i=0;i<k_mer_num[re_cur_kmer];i++) {
			if(readid[k_mer_addr[re_cur_kmer]].a[i][1]>=L-k-2&&readid[k_mer_addr[re_cur_kmer]].a[i][2]==1) {
				int DEC[]=new int[9];
				DEC[0]=readid[k_mer_addr[re_cur_kmer]].a[i][0];
				DEC[1]=-1;
				DEC[2]=readid[k_mer_addr[re_cur_kmer]].a[i][1];
				DEC[3]=readid[k_mer_addr[re_cur_kmer]].a[i][1];
				DEC[4]=readid[k_mer_addr[re_cur_kmer]].a[i][1];
				DEC[5]=1;
				Dec_table.add(DEC);
				readid[k_mer_addr[re_cur_kmer]].a[i][2]=0;
			}
		}
		
		
		
		
	}
	
	
	
	
	
	public int find_next(String cur_kmer_str) {
		//ѡ�����kmer���������-1����˵�����kmer������
		int a=0,b=0,c=0,d=0;
		int next_kmer_num_a,next_kmer_num_b,next_kmer_num_c,next_kmer_num_d;
		int renext_kmer_num_a,renext_kmer_num_b,renext_kmer_num_c,renext_kmer_num_d;
		
		StringBuffer ad=new StringBuffer(cur_kmer_str);
		ad.append("00");
		String next_kmer_str_a=ad.toString().substring(2,ad.toString().length());
		next_kmer_num_a=Integer.valueOf(next_kmer_str_a,2);
		renext_kmer_num_a=Integer.valueOf(reverse(next_kmer_num_a),2);
		
		
		StringBuffer bd=new StringBuffer(cur_kmer_str);
		bd.append("01");
		String next_kmer_str_b=bd.toString().substring(2,bd.toString().length());
		next_kmer_num_b=Integer.valueOf(next_kmer_str_b,2);
		renext_kmer_num_b=Integer.valueOf(reverse(next_kmer_num_b),2);
		
		
		StringBuffer cd=new StringBuffer(cur_kmer_str);
		cd.append("11");
		String next_kmer_str_c=cd.toString().substring(2,cd.toString().length());
		next_kmer_num_c=Integer.valueOf(next_kmer_str_c,2);
		renext_kmer_num_c=Integer.valueOf(reverse(next_kmer_num_c),2);
		
		
		StringBuffer dd=new StringBuffer(cur_kmer_str);
		dd.append("10");
		String next_kmer_str_d=dd.toString().substring(2,dd.toString().length());
		next_kmer_num_d=Integer.valueOf(next_kmer_str_d,2);		
		renext_kmer_num_d=Integer.valueOf(reverse(next_kmer_num_d),2);


		for(int q=0;q<Dec_table.size();q++) {
			int reaid=Dec_table.get(q)[0];
			int proper_pos;
			
			if(Dec_table.get(q)[1]==1&&Dec_table.get(q)[8]==0&&Dec_table.get(q)[3]==Dec_table.get(q)[4]) {
				proper_pos=1+Dec_table.get(q)[3];
				if(0!=k_mer_num[next_kmer_num_a]) {
					for(int p=0;p<k_mer_num[next_kmer_num_a];p++) {
						if(readid[k_mer_addr[next_kmer_num_a]].a[p][0]==reaid
						   &&readid[k_mer_addr[next_kmer_num_a]].a[p][1]==proper_pos
						   &&readid[k_mer_addr[next_kmer_num_a]].a[p][2]==1) {
							a++;
						}
					}
				}
				if(0!=k_mer_num[next_kmer_num_b]) {

					for(int p=0;p<k_mer_num[next_kmer_num_b];p++) {
						if(readid[k_mer_addr[next_kmer_num_b]].a[p][0]==reaid
						   &&readid[k_mer_addr[next_kmer_num_b]].a[p][1]==proper_pos
						   &&readid[k_mer_addr[next_kmer_num_b]].a[p][2]==1) {

							b++;

						}
					}
				}
				if(0!=k_mer_num[next_kmer_num_c]) {
					for(int p=0;p<k_mer_num[next_kmer_num_c];p++) {
						if(readid[k_mer_addr[next_kmer_num_c]].a[p][0]==reaid
						   &&readid[k_mer_addr[next_kmer_num_c]].a[p][1]==proper_pos
						   &&readid[k_mer_addr[next_kmer_num_c]].a[p][2]==1) {
							c++;
						}
					}
				}
				if(0!=k_mer_num[next_kmer_num_d]) {
					for(int p=0;p<k_mer_num[next_kmer_num_d];p++) {
						if(readid[k_mer_addr[next_kmer_num_d]].a[p][0]==reaid
						   &&readid[k_mer_addr[next_kmer_num_d]].a[p][1]==proper_pos
						   &&readid[k_mer_addr[next_kmer_num_d]].a[p][2]==1) {
							d++;
						}
					}
				}
			}else if(Dec_table.get(q)[1]==-1&&Dec_table.get(q)[8]==0&&Dec_table.get(q)[3]==Dec_table.get(q)[4]){
				proper_pos=Dec_table.get(q)[3]-1;
				if(0!=k_mer_num[renext_kmer_num_a]) {
					for(int p=0;p<k_mer_num[renext_kmer_num_a];p++) {
						if(readid[k_mer_addr[renext_kmer_num_a]].a[p][0]==reaid
						   &&readid[k_mer_addr[renext_kmer_num_a]].a[p][1]==proper_pos
						   &&readid[k_mer_addr[renext_kmer_num_a]].a[p][2]==1) {
							a++;
						}
					}
				}
				if(0!=k_mer_num[renext_kmer_num_b]) {
					for(int p=0;p<k_mer_num[renext_kmer_num_b];p++) {
						if(readid[k_mer_addr[renext_kmer_num_b]].a[p][0]==reaid
						   &&readid[k_mer_addr[renext_kmer_num_b]].a[p][1]==proper_pos
						   &&readid[k_mer_addr[renext_kmer_num_b]].a[p][2]==0) {
							b++;

						}
					}
				}
				if(0!=k_mer_num[renext_kmer_num_c]) {
					for(int p=0;p<k_mer_num[renext_kmer_num_c];p++) {
						if(readid[k_mer_addr[renext_kmer_num_c]].a[p][0]==reaid
						   &&readid[k_mer_addr[renext_kmer_num_c]].a[p][1]==proper_pos
						   &&readid[k_mer_addr[renext_kmer_num_c]].a[p][2]==1) {
							c++;

						}
					}
				}
				if(0!=k_mer_num[renext_kmer_num_d]) {
					for(int p=0;p<k_mer_num[renext_kmer_num_d];p++) {
						if(readid[k_mer_addr[renext_kmer_num_d]].a[p][0]==reaid
						   &&readid[k_mer_addr[renext_kmer_num_d]].a[p][1]==proper_pos
						   &&readid[k_mer_addr[renext_kmer_num_d]].a[p][2]==1) {
							d++;

						}
					}
				}
			}
		}
		System.out.print(a);System.out.print(" ");System.out.println(next_kmer_num_a);
		System.out.print(b);System.out.print(" ");System.out.println(next_kmer_num_b);
		System.out.print(c);System.out.print(" ");System.out.println(next_kmer_num_c);
		System.out.print(d);System.out.print(" ");System.out.println(next_kmer_num_d);
		if(a==0&&b==0&&c==0&&d==0) {
			stopcode=1;
			for(int y=0;y<(int)1e8;y++) {
				for(int t=0;t<k_mer_num[y];t++) {
					if(readid[k_mer_addr[y]].a[t][2]==0) {
						readid[k_mer_addr[y]].a[t][2]=1;
					}
				}
			}
			Dangqianshuliang=0;
			return -1;
		}

		else if(a==Math.max(Math.max(a, b),Math.max(c, d))){
			Dangqianshuliang=a;
			return next_kmer_num_a;
		}
		else if(b==Math.max(Math.max(a, b),Math.max(c, d))){
			Dangqianshuliang=b;
			return next_kmer_num_b;
		}
		else if(c==Math.max(Math.max(a, b),Math.max(c, d))){
			Dangqianshuliang=c;
			return next_kmer_num_c;
		}
		
		else {
			Dangqianshuliang=d;
			return next_kmer_num_d;
		}
	}
	
	
	

	
	
	public static void Main() throws IOException {
		DBG dbg=new DBG();
		dbg.read(input);
		if_contig_finished=0;
		while(true) {
			dbg.Contig();
			
			
			
	        int reads_remain_value=0;
			int[] reads_remain=new int[dbg.read_num];
			for(int h=0;h<(int)1e8;h++) {
				for(int y=0;y<dbg.k_mer_num[h];y++) {
					if(dbg.readid[dbg.k_mer_addr[h]].a[y][2]!=-1) {
						reads_remain[dbg.readid[dbg.k_mer_addr[h]].a[y][0]-1]=1;
					}
				}
			}
			for(int h=0;h<dbg.read_num;h++) {
				if(reads_remain[h]!=0) {
					reads_remain_value++;
				}
			}
			
			
			
            /*
			for(int c=0;c<dbg.Dec_table.size();c++) {
				if(dbg.Dec_table.get(c)[8]==100) {
					break;
				}else {
					drop=1;
				}
			}
			*/
			if(reads_remain_value<5||dbg.end==1) {
				break;
			}
			contig_value++;
			//break;
/*
			Scanner _i=new Scanner(System.in);
			System.out.println("STOP?");
			if (_i.next().equals("Y")) {
				break;
			}
			
			*/
		}
		if_contig_finished=1;
		if_blast_finished=0;
		Contigs=new Object[contig_value][3];
		for(int m=0;m<contig_value;m++) {
			Contigs[m][0]="Contig"+String.valueOf(m);
			BufferedReader file=new BufferedReader(new InputStreamReader(new FileInputStream(outpass+m+output_pos)));
			Contigs[m][1]=file.readLine().length();
			file.close();

		}
		
		//Scanner i=new Scanner(System.in);
		//System.out.println("BLAST or not?(Y/N)");
		if (blast_or_not==1) {
			String out=output_pos;
			for(int a=0;a<contig_value;a++) {
				output_pos=String.valueOf(a)+out;
				Blast n=new Blast();
				n.BLAST();
			}
			output_pos=out;
			for(int m=0;m<contig_value;m++) {

				BufferedReader file=new BufferedReader(new InputStreamReader(new FileInputStream(outpass+"BLAST_"+m+output_pos)));
				double fenmu=file.readLine().length();
				String tongji=file.readLine();
				double fenzi=0;
				for(int y=0;y<fenmu;y++) {
					if(tongji.charAt(y)=='*') {
					}else {
						fenzi+=1;
					}
				}
				Contigs[m][2]=fenzi/fenmu;
				file.close();

			}
			if_blast_finished=1;
		}else {
		}
		//i.close();
		
		
	}
	
    public String num_to_ATGC(String _base) {
    	String r="";
    	for(int b=0;b<_base.length();b+=2) {
    		String base=_base.substring(b, b+2);
    		StringBuffer addr=new StringBuffer(r);
    	
    		if(base.equals("00")) {
    			addr.append("A");
    		}
    		if(base.equals("01")) {
    			addr.append("C");
    		}
    		if(base.equals("10")) {
    			addr.append("G");
    		}
    		if(base.equals("11")) {
    			addr.append("T");
    		}
    		r=addr.toString();
    	}
    	return r;
    
    	
    }
	

}

