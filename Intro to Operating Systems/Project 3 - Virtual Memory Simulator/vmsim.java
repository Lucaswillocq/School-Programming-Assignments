import java.util.*;
import java.lang.*;
import java.io.*;

public class vmsim
{


	public static void main(String [] args) throws FileNotFoundException
	{

		String alg_type="";
		String trace_type="";
		int num_of_frames=0;
		String rawpagesize="";
		long pagesize=0;
		int total_memory_portions=0;
		int first_process_portion=0;
		int second_process_portion=0;

		String memory_ratio = "";

		int first_process_frames=0;
		int second_process_frames=0;


		//search through commandline arguments looking for variables
		for(int i=0; i<args.length; i++)
		{
			if(args[i].equals("-a"))
				{alg_type = args[i+1];}

			else if(args[i].equals("-n"))
				{num_of_frames = Integer.parseInt(args[i+1]);}

			else if(args[i].equals("-p"))
				{
					//convert pagesize from KB to bytes
					rawpagesize = (args[i+1]);
					pagesize = (1024)*Long.parseLong(args[i+1]);
				}

			else if(args[i].equals("-s"))
				{memory_ratio = args[i+1];}
		}

		//trace will always be last
		trace_type = args[args.length-1];

		//parse ratio string to get individual portions per process
		String[] ratios = memory_ratio.split(":");
		first_process_portion = Integer.parseInt(ratios[0]);
		second_process_portion = Integer.parseInt(ratios[1]);
		total_memory_portions = (first_process_portion + second_process_portion);
		first_process_frames =  (first_process_portion*num_of_frames)/total_memory_portions;
		second_process_frames = num_of_frames - first_process_frames;
		

		//determine what algorithm to run
		if(alg_type.equalsIgnoreCase("opt"))
			OPT(num_of_frames,pagesize,first_process_frames,second_process_frames,trace_type,rawpagesize);
		else if(alg_type.equalsIgnoreCase("lru"))
			LRU(num_of_frames,pagesize,first_process_frames,second_process_frames,trace_type,rawpagesize);

	}



	public static void LRU(int totalframes, long pagesize, int first_frames, int second_frames, String trace, String rawpagesize) throws FileNotFoundException
	{

		//variables for statistics
		int pagefaults = 0;
		int mem_access = 0;
		int disk_writes = 0;


		//total memory space for both processes
		long[] first_frames_list = new long[first_frames];

		for(int i=0; i< first_frames; i++)
		{
			first_frames_list[i] = -1;
		}

		long[] second_frames_list = new long[second_frames];

		for(int i=0; i<second_frames; i++)
		{
			second_frames_list[i] = -1;
		}

		
		//keep track of amount of frames per process
		int frames_loaded_1 = 0;
		int frames_loaded_2 = 0;


		//calculate # of entries in page table (address space / page size)
		long address_bytes = (long) Math.pow(2,32);
		long pagetableentries = (long) address_bytes/pagesize;


		//generate two page tables, one for each process
		Hashtable<Long, PTE> pagetable_1 = new Hashtable<Long, PTE>();
		Hashtable<Long, PTE> pagetable_2 = new Hashtable<Long, PTE>();

		for(long i=0; i<pagetableentries; i++)
		{
			PTE entry = new PTE();
			entry.index = i;
			pagetable_1.put(i,entry);
		}

		for(long i=0; i<pagetableentries; i++)
		{
			PTE entry = new PTE();
			entry.index = i;
			pagetable_2.put(i,entry);
		}


		//calculate offset bits (log base 2 of pagesize)
		long offset_bits = (long) ((Math.log(pagesize)) / (Math.log(2)));


		//variables for parsing trace file lines
		String access_type;
		long pagenumber;
		int process;
		long pageaddress;

		//counter variable to track priority of pages
		int counter = 0;
 

		Scanner trace_scan = new Scanner(new File(trace));

		while(trace_scan.hasNextLine())
		{
			mem_access++;
			counter++;

			//extract page number, type of access, and which process the trace belongs to
			String memory_line[] = trace_scan.nextLine().split(" ");

			access_type = memory_line[0];

			memory_line[1] = memory_line[1].substring(2);

			pageaddress = Long.parseLong(memory_line[1],16);

			pagenumber = (long) pageaddress>>offset_bits;

			process = Integer.parseInt(memory_line[2]);

			//check to see which process this memory access refers to
			if(process==0)
			{	
				PTE p = pagetable_1.get(pagenumber);

				//check to see if there is open frames
				if(frames_loaded_1<first_frames)
				{

					// if frame isn't valid, then it's not in memory -> PAGEFAULT + add to memory
					if(p.valid==false)
					{

						if(access_type.equals("s"))
							{p.dirty=true;}

						//physical memory frame associated with page entr
						p.frame = frames_loaded_1;

						p.counter = counter;


						//physical memory frame now holds page number of this process
						first_frames_list[frames_loaded_1] = p.index;

						//increase oage faults, and amount of occupied frames in memory
						frames_loaded_1++;
						pagefaults++;

						//page is now in memory (valid)
						p.valid = true;

					}
					// frame is valid, meaning it is in memory -> no PAGEFAULT, a page hit
					else
					{
						if(access_type.equals("s"))
							{p.dirty=true;}

						//update counter
						p.counter = counter;
					}
				}

				//there are no open frames (frames_loaded_1==first_frames)
				else
				{
					//check if frame is already in memory
					if(p.valid==true)
					{
						if(access_type.equals("s"))
							{p.dirty=true;}

						p.counter = counter;

					}
					//frame isn't in memory, and no open frames, which means must evict a frame with lowest counter
					else
					{

						int evict_frame = -1;
						int low_counter = -1;

						//search through frames in memory looking for one with lowest counter to evict

						for(int i=0; i<first_frames_list.length;i++)
						{
							//first time going through
							if(evict_frame==-1)
							{
								evict_frame = i;
								low_counter = pagetable_1.get(first_frames_list[i]).counter;
							}

							else if(pagetable_1.get(first_frames_list[i]).counter<low_counter)
							{
								evict_frame = i;
								low_counter = pagetable_1.get(first_frames_list[i]).counter;
							}

						}

						PTE evicted_frame = pagetable_1.get(first_frames_list[evict_frame]);

						//found frame to evict, now check if need to write to disk
						if(evicted_frame.dirty)
							{disk_writes++;}

						//clean evicted PTE
						evicted_frame.dirty = false;
						evicted_frame.valid = false;
						evicted_frame.frame = -1;

						//set new PTE property
						first_frames_list[evict_frame] = p.index;

						if(access_type.equals("s"))
							{p.dirty=true;}

						p.valid = true;
						p.frame = evict_frame;
						p.counter = counter;
						pagefaults++;

						//put evicted page with updated eviction qualities back into the table
						pagetable_1.put(evicted_frame.index,evicted_frame);

					}

				}

				//update page table for new memory access
				PTE temp = p;

				pagetable_1.put(temp.index, temp);

			}

			if(process==1) //(identical to if process==0 code, but with different page tables / frame lists)
			{	
				PTE p = pagetable_2.get(pagenumber);

				//check to see if there is open frames
				if(frames_loaded_2<second_frames)
				{

					// if frame isn't valid, then it's not in memory -> PAGEFAULT + add to memory
					if(p.valid==false)
					{

						if(access_type.equals("s"))
							{p.dirty=true;}

						p.frame = frames_loaded_1;
						p.counter = counter;

						second_frames_list[frames_loaded_2] =p.index;

						frames_loaded_2++;
						pagefaults++;

						p.valid = true;

					}
					// frame is valid, meaning it is in memory -> no PAGEFAULT, a page hit
					else
					{
						if(access_type.equals("s"))
							{p.dirty=true;}

						p.counter = counter;
					}
				}

				//there are no open frames (frames_loaded_1==first_frames)
				else
				{
					//check if frame is already in memory
					if(p.valid==true)
					{
						if(access_type.equals("s"))
							{p.dirty=true;}

						p.counter = counter;

					}
					//frame isn't in memory, and no open frames, which means must evict a frame with lowest counter
					else
					{

						int evict_frame = -1;
						int low_counter = -1;

						//search through frames in memory looking for one with lowest counter to evict

						for(int i=0; i<second_frames_list.length;i++)
						{
							if(evict_frame==-1)
							{
								evict_frame = i;
								low_counter = pagetable_2.get(second_frames_list[i]).counter;
							}

							else if(pagetable_2.get(second_frames_list[i]).counter<low_counter)
							{
								evict_frame = i;
								low_counter = pagetable_2.get(second_frames_list[i]).counter;
							}
								
						}

						PTE evicted_frame = pagetable_2.get(second_frames_list[evict_frame]);

						//found frame to evict, now check if need to write to disk
						if(evicted_frame.dirty)
							{disk_writes++;}

						//clean evicted PTE
						evicted_frame.dirty = false;
						evicted_frame.valid = false;
						evicted_frame.frame = -1;


						//set new PTE property
						second_frames_list[evict_frame] = p.index;

						if(access_type.equals("s"))
							{p.dirty=true;}

						p.valid = true;
						p.frame = evict_frame;
						p.counter = counter;
						pagefaults++;

						pagetable_2.put(evicted_frame.index,evicted_frame);

					}

				}

			PTE temp = p;

			pagetable_2.put(temp.index,temp);


			}

		}


		printstats("LRU",totalframes,rawpagesize,mem_access,pagefaults,disk_writes);
	}



//*************************************************************************************************************************************************************

	public static void OPT(int totalframes, long pagesize, int first_frames, int second_frames, String trace, String rawpagesize) throws FileNotFoundException
	{

		//variables for statistics
		int pagefaults = 0;
		int mem_access = 0;
		int disk_writes = 0;


		//total memory space for both processes
		long[] first_frames_list = new long[first_frames];

		for(int i=0; i< first_frames; i++)
		{
			first_frames_list[i] = -1;
		}

		long[] second_frames_list = new long[second_frames];

		for(int i=0; i<second_frames; i++)
		{
			second_frames_list[i] = -1;
		}
		
		//keep track of amount of frames per process
		int frames_loaded_1 = 0;
		int frames_loaded_2 = 0;


		//calculate # of entries in page table (address space / page size)
		long address_bytes = (long) Math.pow(2,32);
		long pagetableentries = address_bytes/pagesize;


		//generate two page tables, one for each process
		Hashtable<Long, PTE> pagetable_1 = new Hashtable<Long, PTE>();
		Hashtable<Long, PTE> pagetable_2 = new Hashtable<Long, PTE>();

		for(long i=0; i<pagetableentries; i++)
		{
			PTE entry = new PTE();
			entry.index = i;
			pagetable_1.put(i,entry);
		}

		for(long i=0; i<pagetableentries; i++)
		{
			PTE entry = new PTE();
			entry.index = i;
			pagetable_2.put(i,entry);
		}


		//calculate offset bits (log base 2 of pagesize)
		long offset_bits = (long) ((Math.log(pagesize)) / (Math.log(2)));


		//variables for parsing trace file lines
		String access_type;
		long pagenumber;
		int process;
		long pageaddress;

		//counter variable to track priority of pages
		int counter = 0;
 

		Scanner trace_scan = new Scanner(new File(trace));


		//generate future page tables for optimization
		Hashtable<Long,LinkedList<Long>> future_table_1 = new Hashtable<Long,LinkedList<Long>>();
		Hashtable<Long,LinkedList<Long>> future_table_2 = new Hashtable<Long,LinkedList<Long>>();
		//populate these hashtables

		long line_counter = 0;

		while(trace_scan.hasNextLine())
		{

			String memory_line[] = trace_scan.nextLine().split(" ");

			memory_line[1] = memory_line[1].substring(2);

			pageaddress = Long.parseLong(memory_line[1],16);

			pagenumber = (long) pageaddress>>offset_bits;

			process = Integer.parseInt(memory_line[2]);

			//if pagenumber not seen before, add it to table and initiate head of future references list
			//if pagenumber seen before, add linecounter to tail of future references list

			if(process==0)
			{

				if(future_table_1.get(pagenumber)==null)
				{
					future_table_1.put(pagenumber, new LinkedList<Long>());
					future_table_1.get(pagenumber).add(line_counter);
				}
				else
				{future_table_1.get(pagenumber).add(line_counter);}

			}

			else
			{

				if(future_table_2.get(pagenumber)==null)
				{
					future_table_2.put(pagenumber, new LinkedList<Long>());
					future_table_2.get(pagenumber).add(line_counter);
				}
				else
				{future_table_2.get(pagenumber).add(line_counter);}

			}

			line_counter++;
		}

		trace_scan = new Scanner(new File(trace));


		//actual scan, same as LRU except for eviction policy
		while(trace_scan.hasNextLine())
		{

			mem_access++;
			counter++;

			String memory_line[] = trace_scan.nextLine().split(" ");

			access_type = memory_line[0];

			memory_line[1] = memory_line[1].substring(2);

			pageaddress = Long.parseLong(memory_line[1],16);

			pagenumber = (long) pageaddress>>offset_bits;

			process = Integer.decode(memory_line[2]);


			//currently on this memory line in the file, get rid of it from future references list		
			if(process==0)
			{future_table_1.get(pagenumber).removeFirst();}

			else
			{future_table_2.get(pagenumber).removeFirst();}


			if(process==0)
			{
				PTE p = pagetable_1.get(pagenumber);
			

				//check to see if there is open frames
				if(frames_loaded_1<first_frames)
				{

					// if frame isn't valid, then it's not in memory -> PAGEFAULT + add to memory
					if(p.valid==false)
					{

						if(access_type.equals("s"))
							{p.dirty=true;}

						p.frame = frames_loaded_1;
						p.counter = counter;

						first_frames_list[frames_loaded_1] =p.index;

						frames_loaded_1++;
						pagefaults++;
						p.valid = true;

					}
					// frame is valid, meaning it is in memory -> no PAGEFAULT, a page hit
					else
					{
						if(access_type.equals("s"))
							{p.dirty=true;}

						p.counter = counter;
					}
				}

				//there are no open frames (frames_loaded_1==first_frames)
				else
				{
					//check if frame is already in memory
					if(p.valid==true)
					{
						if(access_type.equals("s"))
							{p.dirty=true;}

						p.counter = counter;

					}
					//frame isn't in memory, and no open frames, which means must evict a frame that is used farthest away
					else
					{
						//keeps track of page reference furthest away in history
						long longest = 0;

						PTE evicted_frame = new PTE();

						//keeps track of next references for the pages in memory, if there are no more references in the future, set value to max
						long[] future_frames = new long[first_frames_list.length];

						int evict_frame = 0;


						//populate list of future references
						for(int i=0; i<first_frames_list.length; i++)
						{
							if(future_table_1.get(first_frames_list[i]).isEmpty())
							{future_frames[i] = Long.MAX_VALUE;}

							else
							{future_frames[i] = future_table_1.get(first_frames_list[i]).get(0);}

						}


						//check if future reference is the furthest away, if theres a tie (max vs max), use counter to choose victim page
						for(int i=0; i<future_frames.length; i++)
						{
							if(future_frames[i]>longest)
							{
								longest = future_frames[i];
								evicted_frame = pagetable_1.get(first_frames_list[i]);
								evict_frame = i;
							}

							else if(future_frames[i]==longest)
							{
								if(pagetable_1.get(first_frames_list[i]).counter<evicted_frame.counter)
								{
									longest = future_frames[i];
									evicted_frame = pagetable_1.get(first_frames_list[i]);
									evict_frame = i;
								}
							}
						}
						

						//found frame to evict, now check if need to write to disk
						if(evicted_frame.dirty)
							{disk_writes++;}

						//clean evicted PTE
						evicted_frame.dirty = false;
						evicted_frame.valid = false;
						evicted_frame.frame = -1;

						first_frames_list[evict_frame] = p.index;


						//set new PTE property

						if(access_type.equals("s"))
							{p.dirty=true;}

						p.valid = true;
						p.frame = evict_frame;
						p.counter = counter;
						pagefaults++;

						pagetable_1.put(evicted_frame.index,evicted_frame);

					}

				}

				PTE temp = p;

				pagetable_1.put(temp.index,temp);

			}


			if(process==1)
			{	
				PTE p = pagetable_2.get(pagenumber);

				//check to see if there is open frames
				if(frames_loaded_2<second_frames)
				{

					// if frame isn't valid, then it's not in memory -> PAGEFAULT + add to memory
					if(p.valid==false)
					{

						if(access_type.equals("s"))
							{p.dirty=true;}

						p.frame = frames_loaded_1;
						p.counter = counter;

						second_frames_list[frames_loaded_2] =p.index;

						frames_loaded_2++;
						pagefaults++;
						p.valid = true;

					}
					// frame is valid, meaning it is in memory -> no PAGEFAULT, a page hit
					else
					{
						if(access_type.equals("s"))
							{p.dirty=true;}

						p.counter = counter;
					}
				}

				//there are no open frames (frames_loaded_1==first_frames)
				else
				{
					//check if frame is already in memory
					if(p.valid==true)
					{
						if(access_type.equals("s"))
							{p.dirty=true;}

						p.counter = counter;

					}
					//frame isn't in memory, and no open frames, which means must evict a frame with lowest counter
					//same implementation as process 1's, with different future and page tables
					else
					{

						long longest = 0;

						PTE evicted_frame = new PTE();

						long[] future_frames = new long[second_frames_list.length];

						int evict_frame = 0;

						for(int i=0; i<second_frames_list.length; i++)
						{
							if(future_table_2.get(second_frames_list[i]).isEmpty())
							{future_frames[i] = Long.MAX_VALUE;}

							else
							{future_frames[i] = future_table_2.get(second_frames_list[i]).get(0);}

						}

						for(int i=0; i<future_frames.length; i++)
						{
							if(future_frames[i]>longest)
							{
								longest = future_frames[i];
								evicted_frame = pagetable_2.get(second_frames_list[i]);
								evict_frame = i;
							}

							else if(future_frames[i]==longest)
							{
								if(pagetable_2.get(second_frames_list[i]).counter<evicted_frame.counter)
								{
									longest = future_frames[i];
									evicted_frame = pagetable_2.get(second_frames_list[i]);
									evict_frame = i;
								}
							}
						}


						//found frame to evict, now check if need to write to disk
						if(evicted_frame.dirty)
							{disk_writes++;}

						//clean evicted PTE
						evicted_frame.dirty = false;
						evicted_frame.valid = false;
						evicted_frame.frame = -1;


						//set new PTE property
						second_frames_list[evict_frame] = p.index;

						if(access_type.equals("s"))
							{p.dirty=true;}

						p.valid = true;
						p.frame = evict_frame;
						p.counter = counter;
						pagefaults++;

						pagetable_2.put(evicted_frame.index,evicted_frame);

					}

				}

				PTE temp = p;

				pagetable_2.put(temp.index,temp);


			}

		}


		printstats("OPT",totalframes,rawpagesize,mem_access,pagefaults,disk_writes);

	}

	public static void printstats(String algorithm, int frames, String rawpagesize, int memaccess, int pagefault, int diskwrites)
	{
		System.out.println("Algorithm: "+algorithm);

		System.out.println("Number of frames: "+frames);

		System.out.println("Page size: "+rawpagesize+" KB");

		System.out.println("Total memory accesses: "+memaccess);

		System.out.println("Total page faults: "+pagefault);

		System.out.println("Total writes to disk: "+diskwrites);


	}




}
