#define FUSE_USE_VERSION 26

#include <errno.h>
#include <fcntl.h>
#include <fuse.h>
#include <stdio.h>
#include <string.h>

#include "cs1550.h"

/**
 * Called whenever the system wants to know the file attributes, including
 * simply whether the file exists or not.
 *
 * `man 2 stat` will show the fields of a `struct stat` structure.
 */

//for init and destroy
static FILE* disk;

static int cs1550_getattr(const char *path, struct stat *statbuf){
	
	// Clear out `statbuf` first -- this function initializes it.
	memset(statbuf, 0, sizeof(struct stat));

	// Check if the path is the root directory. (included in skeleton)
	if (strcmp(path, "/") == 0) {
		statbuf->st_mode = S_IFDIR | 0755;
		statbuf->st_nlink = 2;
		return 0; // no error
	}
	
	//variables for directory name, file name, and extension name (if there)
	char directory[MAX_FILENAME+1];
	char filename[MAX_FILENAME+1];
	char extension[MAX_EXTENSION+1];

	//intialize variables
	memset(filename, 0, MAX_FILENAME+1);
	memset(extension, 0, MAX_EXTENSION+1);
	memset(directory, 0, MAX_FILENAME+1);

	//pointer for disk
	FILE* diskptr = fopen(".disk", "rb+");
	
	//return value for sscanf
	int ret = 0;
	
	ret = sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);

	//check for length of names (dont need this)
	//if(strlen(directory)>MAX_FILENAME || strlen(filename)>MAX_FILENAME || strlen(extension)>MAX_EXTENSION)
	//{return -ENAMETOOLONG;}
	
	//path is a subdirectory
	if(ret==1)
	{
		//read root directory from disk
		fseek(diskptr,0,SEEK_SET);
		struct cs1550_root_directory root_dir;
		fread(&root_dir, sizeof(struct cs1550_root_directory),1,diskptr);
		
		//search through directories in root, if name matches one in path, know we've found the subdirectory
		for(size_t i=0; i<root_dir.num_directories; i++)
		{
			struct cs1550_directory curr_dir = root_dir.directories[i];
			if(strcmp(curr_dir.dname,directory)==0)
			{
				statbuf->st_mode = S_IFDIR | 0755;
	 			statbuf->st_nlink = 2;
	 			//close diskptr
	 			fclose(diskptr);
	 			return 0; // no error
			}
		}
		
		fclose(diskptr);
		//if we get here, we didn't find the subdirectory
		return -ENOENT;
		
	}
	//if sscanf had a path with a file (or with an extension)
	else if(ret==2 || ret==3)
	{
		//once again, read root directory 
		fseek(diskptr,0,SEEK_SET);
		struct cs1550_root_directory root_dir;
		fread(&root_dir, sizeof(struct cs1550_root_directory),1,diskptr);
		
		//search for matching subdirectory name
		for(size_t i=0; i<root_dir.num_directories; i++)
		{
			struct cs1550_directory curr_dir = root_dir.directories[i];
			if(strcmp(curr_dir.dname,directory)==0)
			{	//directory found, get starting block of it
				size_t index_block = curr_dir.n_start_block;

				//move pointer in .disk to spot of directory
				fseek(diskptr,index_block*BLOCK_SIZE,SEEK_SET);
				//read subdirectory block
				struct cs1550_directory_entry sub_dir;
				fread(&sub_dir,sizeof(struct cs1550_directory_entry),1, diskptr);

				//iterate through files in directory to see if one matches name with path
				
				for(size_t j=0; j< sub_dir.num_files; j++)
				{
						struct cs1550_file_entry sub_file = sub_dir.files[j];
						if(strcmp(sub_file.fname,filename)==0 && ret==2)
						{
							// Regular file
	 						statbuf->st_mode = S_IFREG | 0666;
	 						// Only one hard link to this file
	 						statbuf->st_nlink = 1;
	 						// File size -- replace this with the real size
	 						statbuf->st_size = sub_file.fsize;

	 						fclose(diskptr);
	 						return 0; // no error
						}
						//if extension was given
						if(strcmp(sub_file.fname,filename)==0 && strcmp(sub_file.fext,extension)==0)
						{
							// Regular file
	 						statbuf->st_mode = S_IFREG | 0666;
	 						// Only one hard link to this file
	 						statbuf->st_nlink = 1;
	 						// File size -- replace this with the real size
	 						statbuf->st_size = sub_file.fsize;

	 						fclose(diskptr);
	 						return 0; // no error
						}	
				}
			}
		}
		fclose(diskptr);
		//couldn't find the subdirectory
		return -ENOENT;
	}
	fclose(diskptr);
	//should never reach here, but in case, its an error
	return -ENOENT;
}

/**
 * Called whenever the contents of a directory are desired. Could be from `ls`,
 * or could even be when a user presses TAB to perform autocompletion.
 */
static int cs1550_readdir(const char *path, void *buf, fuse_fill_dir_t filler,
			  off_t offset, struct fuse_file_info *fi)
{
	//dont need these 
	(void) offset;
	(void) fi;

	//variables for parsing path
	char directory[MAX_FILENAME+1];
	char filename[MAX_FILENAME+1];
	char extension[MAX_EXTENSION+1];

	memset(filename, 0, MAX_FILENAME+1);
	memset(extension, 0, MAX_EXTENSION+1);
	memset(directory, 0, MAX_FILENAME+1);

	
	//for # of parameters in path
	//int ret = 0;
	
	sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);

	//check for length of names
	//if(strlen(directory)>MAX_FILENAME || strlen(filename)>MAX_FILENAME || strlen(extension)>MAX_EXTENSION)
	//{return -ENAMETOOLONG;}
	
	//if path wasn't for root or a subdirectory
	//if(ret!=0 && ret!=1)
		//return -EPERM;
	
	//load root directory
	FILE* diskptr = fopen(".disk","rb+");
	fseek(diskptr,0,SEEK_SET);
	struct cs1550_root_directory root_dir;
	fread(&root_dir, sizeof(struct cs1550_root_directory),1,diskptr);
	
	// The filler function allows us to add entries to the listing.
	filler(buf, ".", NULL, 0);
	filler(buf, "..", NULL, 0);
	

	//if path was just root, fill buffer with names of all subdirectories and return
	if(strcmp(path,"/")==0)
	{
		for(size_t i=0; i<root_dir.num_directories; i++)
		{
			//strcpy(directory,root_dir.directories[i].dname);
			filler(buf,(root_dir.directories[i].dname), NULL, 0);
		}
		fclose(diskptr);
		return 0;
	}
	//if path was subdirectory, fill buffer with names of all files and their extensions (if there)
	else
	{
		//if can't find subdirectory, doesn't exist.  Otherwise, find it's starting block
		size_t block_num = -1;
		int found = 0;
		for(size_t i=0; i<root_dir.num_directories; i++)
		{
			struct cs1550_directory curr_dir = root_dir.directories[i];
			if(strcmp(curr_dir.dname,directory)==0)
			{
				block_num = curr_dir.n_start_block;
				found=1;
				break;
			}
		}
		if(found!=1)
		{
			fclose(diskptr);
			return -ENOENT;
		}

		//seek to spot of subdirectory on disk, load subdirectory 
		fseek(diskptr, block_num*BLOCK_SIZE,SEEK_SET);
		struct cs1550_directory_entry dir;
		fread(&dir, sizeof(struct cs1550_directory_entry),1,diskptr);

		//iterate through files in subdirectory, adding them to buffer
		for(size_t i=0; i<dir.num_files; i++)
		{
			//if there's an extension
			if(strlen(dir.files[i].fext)>0)
			{
				//new variable for filename, length of max filename+extension+.
				char fname[MAX_FILENAME+MAX_EXTENSION+1];
				strcpy(fname,dir.files[i].fname);
				strcat(fname,".");
				strcat(fname,dir.files[i].fext);
				filler(buf,fname, NULL, 0);
			}
			else
			{	//no extension given
				char fname[MAX_FILENAME];
				strcpy(fname,dir.files[i].fname);
				filler(buf,fname+1, NULL, 0);
			}
		}

		//successfully added
		
		fclose(diskptr);
		return 0;
							  
	}
	//some error
	fclose(diskptr);
	return -ENOENT;

}

/**
 * Creates a directory. Ignore `mode` since we're not dealing with permissions.
 */
static int cs1550_mkdir(const char *path, mode_t mode)
{
	//ignore mode
	(void) mode;
	
	
	//variables for path parameters
	char directory[MAX_FILENAME+1];
	char filename[MAX_FILENAME+1];
	char extension[MAX_EXTENSION+1];

	memset(filename, 0, MAX_FILENAME+1);
	memset(extension, 0, MAX_EXTENSION+1);
	memset(directory, 0, MAX_FILENAME+1);

	int ret;
	//get total parameters in path
 	ret = sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);
	
	//can't make directory in a directory
	if(ret!=1 )
	{return -EPERM;}

	//make sure directory name not too long
	if(strlen(directory)>MAX_FILENAME)
	{return -ENAMETOOLONG;}
	
	//load root directory
	FILE* diskptr = fopen(".disk","rb+");

	fseek(diskptr,0,SEEK_SET);
	struct cs1550_root_directory root_dir;
	fread(&root_dir, BLOCK_SIZE, 1, diskptr);
	
	//if max amount of subdirectories, can't add any more
	if(root_dir.num_directories==MAX_DIRS_IN_ROOT)
	{
		fclose(diskptr);
		return -ENOSPC;
	}
	
	//look through subdirectories, if any have same name, already exists
	for(size_t i=0; i<root_dir.num_directories; i++)
	{
		struct cs1550_directory curr_dir = root_dir.directories[i];
		if(strcmp(curr_dir.dname,directory)==0)
		{
			fclose(diskptr);
			return -EEXIST;
		}
	}
	
	//at this point, ready to add new subdirectory

	//variable for number of directories
	size_t num_directories = root_dir.num_directories;

	//need two new directory structures, one for table, one actual entry
	struct cs1550_directory new_dir;

	//table directory needs name and start block
	strcpy(new_dir.dname,directory);
	new_dir.n_start_block = root_dir.last_allocated_block+1;

	//keep track of start block for new directory
	size_t dir_index = new_dir.n_start_block*BLOCK_SIZE;

	//put table directory into rootdir
	root_dir.directories[num_directories] = new_dir;

	//for actual directory block
	struct cs1550_directory_entry new_dir_entry;

	//set files to 0
	new_dir_entry.num_files=0;
	
	//increment last allocated block in root
	root_dir.last_allocated_block++;
	
	//increment num of directories in root 
	root_dir.num_directories++;
	
	//write back the changes to the root directory
	
	fseek(diskptr,0,SEEK_SET);
	
	fwrite(&root_dir, sizeof(struct cs1550_root_directory),1,diskptr);

	//write back the changes to new directory entry
	fseek(diskptr,dir_index,SEEK_SET);

	fwrite(&new_dir_entry,sizeof(struct cs1550_directory_entry),1,diskptr);
	
	fclose(diskptr);
	
	return 0;
		
}

/**
 * Removes a directory.
 */
static int cs1550_rmdir(const char *path)
{
	(void) path;
	
	//Don't need to implement this

	return 0;
}

/**
 * Does the actual creation of a file. `mode` and `dev` can be ignored.
 */
static int cs1550_mknod(const char *path, mode_t mode, dev_t dev)
{
	//can ignore for project
	(void) mode;
	(void) dev;
	
	//variables for path parameters
	char directory[MAX_FILENAME + 1];
	char filename[MAX_FILENAME + 1];
	char extension[MAX_EXTENSION + 1];

	memset(filename, 0, MAX_FILENAME+1);
	memset(extension, 0, MAX_EXTENSION+1);
	memset(directory, 0, MAX_FILENAME+1);


	int ret;

	ret = sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);

	//not a file
	if(ret!=2 && ret!=3)
		{return -EPERM;}

	//check for length of names
	if(strlen(filename)>MAX_FILENAME || strlen(extension)>MAX_EXTENSION)
	{return -ENAMETOOLONG;}
	
	//load root directory
	FILE* diskptr = fopen(".disk", "rb+");

	fseek(diskptr,0,SEEK_SET);
	struct cs1550_root_directory root_dir;
	fread(&root_dir, sizeof(struct cs1550_root_directory),1,diskptr);
	
	//iterate through root directory, looking for subdirectory that matches path
	for(size_t i=0; i<root_dir.num_directories; i++)
	{
		struct cs1550_directory curr_dir = root_dir.directories[i];
		if(strcmp(curr_dir.dname,directory)==0)
		{
			//found right subdirectory, now find it's index block
			size_t index_block = curr_dir.n_start_block;

			//move pointer in .disk to spot of directory
			fseek(diskptr, index_block*BLOCK_SIZE, SEEK_SET);
			//read subdirectory block
			struct cs1550_directory_entry sub_dir;

			fread(&sub_dir,sizeof(struct cs1550_directory_entry),1,diskptr);
			
			//if max files, can't add any more
			if(sub_dir.num_files==MAX_FILES_IN_DIR)
			{
				fclose(diskptr);
				return -ENOSPC;
			}
			
			//look through files in subdirectory to see if this files already exists
			for(size_t j=0; j<sub_dir.num_files; j++)
			{
				struct cs1550_file_entry sub_file = sub_dir.files[j];
				if(strcmp(sub_file.fname,filename)==0 && strcmp(sub_file.fext,extension)==0)
				{
					fclose(diskptr);
					return -EEXIST;
				}
			}
			
			//file doesn't exist, make new file entry, new index block, and new data block
			struct cs1550_file_entry new_file;

			//also need new index block
			struct cs1550_index_block new_index_block;

			//need new data block
			struct cs1550_data_block new_data_block;


			//set filename (and extension if possible)
			strcpy(new_file.fname,filename);
			if(ret==3)
			{strcpy(new_file.fext,extension);}

			//set index block of file
			new_file.n_index_block = root_dir.last_allocated_block+1;

			//set filesize to 0 initally
			new_file.fsize = 0;

			//place new file entry into list of files
			size_t num_files = sub_dir.num_files;
			sub_dir.files[num_files] = new_file;

			//update directory structure
			sub_dir.num_files++;

			//update root directories last allocated
			root_dir.last_allocated_block++;

			//set first block in entry to last allocated block
			new_index_block.entries[0]=root_dir.last_allocated_block+1;

			//location will be at the last allocated block
			size_t new_data_block_index = root_dir.last_allocated_block*BLOCK_SIZE;

			//update last allocated block
			root_dir.last_allocated_block++;

			//write all of these back to the disk
			fseek(diskptr,0,SEEK_SET);
			fwrite(&root_dir, sizeof(struct cs1550_root_directory),1,diskptr);

			//variable to remember index of sub directory entry
			size_t sub_dir_index = index_block*BLOCK_SIZE;

			fseek(diskptr,sub_dir_index,SEEK_SET);
			fwrite(&sub_dir,sizeof(struct cs1550_directory_entry),1,diskptr);

			//variable to remember index of index block
			size_t index_block_index = new_file.n_index_block*BLOCK_SIZE;

			fseek(diskptr,index_block_index,SEEK_SET);
			fwrite(&new_index_block,sizeof(struct cs1550_index_block),1,diskptr);

			//write data block back
			fseek(diskptr,new_data_block_index,SEEK_SET);
			fwrite(&new_data_block,sizeof(struct cs1550_data_block),1,diskptr);

			fclose(diskptr);

			return 0;
		
		}
	}
	//if we get here, didn't find the subdirectory
	fclose(diskptr);
	return -ENOENT;
}

/**
 * Deletes a file.
 */
static int cs1550_unlink(const char *path)
{
	(void) path;
	
	//Don't need to implement this
	return 0;
}

/**
 * Read `size` bytes from file into `buf`, starting from `offset`.
 */
static int cs1550_read(const char *path, char *buf, size_t size, off_t offset,
		       struct fuse_file_info *fi)
{
	(void)fi;


	//variables for parsing path
	char directory[MAX_FILENAME+1];
	char filename[MAX_FILENAME+1];
	char extension[MAX_EXTENSION+1];

	memset(filename, 0, MAX_FILENAME+1);
	memset(extension, 0, MAX_EXTENSION+1);
	memset(directory, 0, MAX_FILENAME+1);

	
	//for # of parameters in path
	int ret;
	
	ret = sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);

	//check for length of names
	if(strlen(directory)>MAX_FILENAME || strlen(filename)>MAX_FILENAME || strlen(extension)>MAX_EXTENSION)
	{return -ENAMETOOLONG;}
	
	//if path wasn't for a file
	if(ret!=2 && ret!=3)
		return -EISDIR;
	
	//load root directory
	FILE* diskptr = fopen(".disk", "rb+");
	fseek(diskptr,0,SEEK_SET);
	struct cs1550_root_directory root_dir;
	fread(&root_dir, sizeof(struct cs1550_root_directory),1,diskptr);


	//search through directories in root, if name matches one in path, know we've found the subdirectory
	for(size_t i=0; i<root_dir.num_directories; i++)
	{
		struct cs1550_directory curr_dir = root_dir.directories[i];
		if(strcmp(curr_dir.dname,directory)==0)
		{
			
			//found right subdirectory, now find it's index block
			size_t index_block = curr_dir.n_start_block;

			//move pointer in .disk to spot of directory
			fseek(diskptr, index_block*BLOCK_SIZE, SEEK_SET);
			//read subdirectory block
			struct cs1550_directory_entry sub_dir;
			fread(&sub_dir,sizeof(struct cs1550_directory_entry),1,diskptr);

			//look through files in subdirectory to see if this files already exists
			for(size_t j=0; j<sub_dir.num_files; j++)
			{
				struct cs1550_file_entry sub_file = sub_dir.files[j];
				if(strcmp(sub_file.fname,filename)==0 && strcmp(sub_file.fext,extension)==0)
				{
					//found right file, now find index block
					//variable for index block of file
					size_t index = sub_file.n_index_block;

					//get filesize
					size_t filesize = sub_file.fsize;

					//if the offset + bytes size is greater than the filesize, reduce bytes size to amount of possible bytes read
					if((offset + size) > filesize)
						{size = filesize - offset;}

					//using index block of file, read into index_block structure
					fseek(diskptr,(index*BLOCK_SIZE), SEEK_SET);
					struct cs1550_index_block file_index_block;
					fread(&file_index_block, sizeof(struct cs1550_index_block), 1, diskptr);

					//found file index block, now determine at what data block to start reading
					size_t offset_index = offset/BLOCK_SIZE;

					//this is the block # of the first data block to read
					size_t data_index = file_index_block.entries[offset_index];

					//read this data block
					fseek(diskptr,(data_index*BLOCK_SIZE), SEEK_SET);
					struct cs1550_data_block data_block;
					fread(&data_block, sizeof(struct cs1550_data_block), 1, diskptr);

					//this is where to start within the block, leftover offset 
					size_t new_offset = offset%BLOCK_SIZE;

					//keep track of bytes read so far
					size_t size_read = 0;

					//if there is more than one block of data to be read, read to the end of this block and update
					if(new_offset + size > BLOCK_SIZE)
					{
						//size read during this is distance from offset to end of block
						size_t size_read_so_far = BLOCK_SIZE - new_offset;
						//read this to buffer,starting at the offset position
						memcpy(buf,data_block.data+new_offset,size_read_so_far);
						//update total size read
						size_read += size_read_so_far;
						//increment buffer by amount read
						buf += size_read_so_far;
						//no offset now, at beginning of next block
						new_offset=0;
						//now at next data block,so increment the index in the data block array in the file index block
						data_index++;
						//reload next data block
						fseek(diskptr,(data_index*BLOCK_SIZE), SEEK_SET);
						struct cs1550_data_block data_block;
						fread(&data_block, sizeof(struct cs1550_data_block), 1, diskptr);
					}

					//while the amount of bytes read is less than the amount requested, keep reading
					while(size_read < size)
					{
						//amount of bytes left is possible amount of bytes minus bytes read already
						size_t size_left = size - size_read;
						//if the size left is greater than a block
						if(size_left > BLOCK_SIZE)
						{
							//increment the bytes read by a block size
							size_read += BLOCK_SIZE;
							//read this into memory, starting at new offset (which would be 0 if more than one block, or the calculated offset if just one block)
							memcpy(buf,data_block.data, BLOCK_SIZE);
							//increment buf by block size
							buf+= BLOCK_SIZE;
							//increment block 
							data_index++;
							//load next block
							fseek(diskptr,(data_index*BLOCK_SIZE), SEEK_SET);
							struct cs1550_data_block data_block;
							fread(&data_block, sizeof(struct cs1550_data_block), 1, diskptr);

						}
						//the size left is not greater than a single block, this would be the final step
						else
						{
							memcpy(buf,data_block.data,size_left);
							buf += size_left;
							//size read is incremented by the amount left
							size_read+= size_left;
						}
					}
					fclose(diskptr);
					return size;
				}
		
			}

			//if we get here, we didn't find the file
				fclose(diskptr);
				return -ENOENT;
		}

	}

		//if we get here, we didn't find the subdirectory
	fclose(diskptr);
		return -ENOENT;
	
}

/**
 * Write `size` bytes from `buf` into file, starting from `offset`.
 */
static int cs1550_write(const char *path, const char *buf, size_t size,
			off_t offset, struct fuse_file_info *fi)
{
	
	//very similar to read

	//Don't need for this function
	(void) fi;

	//variables for parsing path
	char directory[MAX_FILENAME+1];
	char filename[MAX_FILENAME+1];
	char extension[MAX_EXTENSION+1];

	memset(filename,0,MAX_FILENAME+1);
	memset(extension,0,MAX_EXTENSION+1);
	memset(directory,0,MAX_FILENAME+1);

	
	//for # of parameters in path
	int ret;
	
	ret = sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);

	//check for length of names
	if(strlen(directory)>MAX_FILENAME || strlen(filename)>MAX_FILENAME || strlen(extension)>MAX_EXTENSION)
	{return -ENAMETOOLONG;}
	
	//if path wasn't for a file
	if(ret!=2 && ret!=3)
		return -EISDIR;
	
	//load root directory
	FILE* diskptr = fopen(".disk", "rb+");
	fseek(diskptr,0,SEEK_SET);
	struct cs1550_root_directory root_dir;
	fread(&root_dir, sizeof(struct cs1550_root_directory),1,diskptr);


	//search through directories in root, if name matches one in path, know we've found the subdirectory
	for(size_t i=0; i<root_dir.num_directories; i++)
	{
		struct cs1550_directory curr_dir = root_dir.directories[i];
		if(strcmp(curr_dir.dname,directory)==0)
		{
			
			//found right subdirectory, now find it's index block
			size_t index_block = curr_dir.n_start_block;

			//move pointer in .disk to spot of directory
			fseek(diskptr, index_block*BLOCK_SIZE, SEEK_SET);
			//read subdirectory block
			struct cs1550_directory_entry sub_dir;
			fread(&sub_dir,sizeof(struct cs1550_directory_entry),1,diskptr);

			//look through files in subdirectory to see if this files already exists
			for(size_t j=0; j<sub_dir.num_files; j++)
			{
				struct cs1550_file_entry sub_file = sub_dir.files[j];
				if(strcmp(sub_file.fname,filename)==0 && strcmp(sub_file.fext,extension)==0)
				{
					//found right file, now find index block

					//variable for index block of file
					size_t index = sub_file.n_index_block;

					//using index block of file, read into index_block structure
					fseek(diskptr,(index*BLOCK_SIZE), SEEK_SET);
					struct cs1550_index_block file_index_block;
					fread(&file_index_block, sizeof(struct cs1550_index_block), 1, diskptr);

					//found file index block, now determine at what data block to start reading
					size_t offset_index = offset/BLOCK_SIZE;

					//this is the block # of the first data block to read
					size_t data_index = file_index_block.entries[offset_index];

					//read this data block
					fseek(diskptr,(data_index*BLOCK_SIZE), SEEK_SET);
					struct cs1550_data_block data_block;
					fread(&data_block, sizeof(struct cs1550_data_block), 1, diskptr);

					//find total blocks for file
					size_t totalblocks = sub_file.fsize/BLOCK_SIZE;

					if(sub_file.fsize%BLOCK_SIZE!=0)
					{totalblocks++;}
	
					//blocks needed to write
					size_t write_blocks = (offset + size)/BLOCK_SIZE;

					if((offset+size)%BLOCK_SIZE!=0)
					{write_blocks++;}
					//extra blocks not already belonging to file
					size_t extra_blocks_needed = write_blocks - totalblocks;
					//add as many extra blocks as needed, or until reach max entries in index
					while(extra_blocks_needed>0 && totalblocks<=MAX_ENTRIES_IN_INDEX_BLOCK)
					{
						
						//last index is now the last allocated block
						file_index_block.entries[totalblocks] = root_dir.last_allocated_block+1;
						//increment last allocated block
						size_t new_data_block_spot = root_dir.last_allocated_block+1;

						root_dir.last_allocated_block++;
						totalblocks++;

						struct cs1550_data_block new_data_block;

						fseek(diskptr,new_data_block_spot*BLOCK_SIZE,SEEK_SET);
						fwrite(&new_data_block,sizeof(struct cs1550_data_block),1,diskptr);
							
						//update these blocks
						fseek(diskptr,0,SEEK_SET);
						fwrite(&root_dir,sizeof(struct cs1550_root_directory),1,diskptr);
						fseek(diskptr,index*BLOCK_SIZE,SEEK_SET);
						fwrite(&file_index_block,sizeof(struct cs1550_index_block),1,diskptr);
						//one less block needed
						extra_blocks_needed--;
					}
					

					//this is where to start within the block, leftover offset 
					size_t new_offset = offset%BLOCK_SIZE;

					//amount of bytes written in so far
					size_t size_written = 0;

					//if there is more than a block of data to be written
					if((new_offset+size)>BLOCK_SIZE)
					{

						//size written during this is distance from offset to end of block
						size_t size_written_so_far = BLOCK_SIZE - new_offset;
						//werite this to buffer,starting at the offset position
						memcpy(data_block.data+new_offset,buf,size_written_so_far);
						//update total size written
						size_written += size_written_so_far;
						//increment buffer by amount written
						buf += size_written_so_far;
						//no offset now, at beginning of next block
						fseek(diskptr,(data_index*BLOCK_SIZE),SEEK_SET);
						fwrite(&data_block,sizeof(struct cs1550_data_block),1,diskptr);

						new_offset=0;
						//now at next data block,so increment the index in the data block array in the file index block
						data_index++;
						//reload next data block
						fseek(diskptr,(data_index*BLOCK_SIZE), SEEK_SET);
						struct cs1550_data_block data_block;
						fread(&data_block, sizeof(struct cs1550_data_block), 1, diskptr);
					}

					//while the amount of bytes read is less than the amount requested, keep reading
					while(size_written < size)
					{
						//amount of bytes left is possible amount of bytes minus bytes read already
						size_t size_left = size - size_written;
						//if the size left is greater than a block
						if(size_left > BLOCK_SIZE)
						{
							//increment the bytes read by a block size
							size_written += BLOCK_SIZE;
							//read this into memory, starting at new offset (which would be 0 if more than one block, or the calculated offset if just one block)
							memcpy(data_block.data,buf,BLOCK_SIZE);
							//increment buf by block size
							buf+= BLOCK_SIZE;

							fseek(diskptr,(data_index*BLOCK_SIZE),SEEK_SET);
							fwrite(&data_block,sizeof(struct cs1550_data_block),1,diskptr);

							//increment block 
							data_index++;
							//load next block
							fseek(diskptr,(data_index*BLOCK_SIZE), SEEK_SET);
							struct cs1550_data_block data_block;
							fread(&data_block, sizeof(struct cs1550_data_block), 1, diskptr);

						}
						//the size left is not greater than a single block, this would be the final step
						else
						{
							memcpy(data_block.data,buf,size_left);
							buf += size_left;
							//size read is incremented by the amount left
							fseek(diskptr,(data_index*BLOCK_SIZE),SEEK_SET);
							fwrite(&data_block,sizeof(struct cs1550_data_block),1,diskptr);


							size_written+= size_left;
						}
					}

					sub_dir.files[j].fsize+=size_written;
					fseek(diskptr, index_block*BLOCK_SIZE, SEEK_SET);
					fwrite(&sub_dir,sizeof(struct cs1550_directory_entry),1,diskptr);
					fclose(diskptr);

					return size;

				}

			}
			fclose(diskptr);
			return -ENOENT;
		}
	}
	fclose(diskptr);
	return -ENOENT;

}

/**
 * Called when a new file is created (with a 0 size) or when an existing file
 * is made shorter. We're not handling deleting files or truncating existing
 * ones, so all we need to do here is to initialize the appropriate directory
 * entry.
 */
static int cs1550_truncate(const char *path, off_t size)
{
	(void) path;
	(void) size;
	
	//Don't need to implement this
	return 0;
}

/**
 * Called when we open a file.
 */
static int cs1550_open(const char *path, struct fuse_file_info *fi)
{
	
	(void) fi;
	
	char directory[MAX_FILENAME+1];
	char filename[MAX_FILENAME+1];
	char extension[MAX_EXTENSION+1];

	memset(filename, 0, MAX_FILENAME+1);
	memset(extension, 0, MAX_EXTENSION+1);
	memset(directory, 0, MAX_FILENAME+1);

	
	
	//return value for sscanf
	int ret= 0;
	
	ret = sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);

	//check for length of names
	//if(strlen(directory)>MAX_FILENAME || strlen(filename)>MAX_FILENAME || strlen(extension)>MAX_EXTENSION)
	//{return -ENAMETOOLONG;}
	
	//root directory, valid
	if(ret==0)
	{return 0;}
	
	//load root directory
	FILE* diskptr = fopen(".disk", "rb+");

	fseek(diskptr,0,SEEK_SET);
	struct cs1550_root_directory root_dir;
	fread(&root_dir, sizeof(struct cs1550_root_directory),1,diskptr);
		
	//search through directories in root, if name matches one in path, know we've found the subdirectory
	for(size_t i=0; i<root_dir.num_directories;i++)
	{
		//if looking for a subdirectory and name matches, return success
		struct cs1550_directory curr_dir = root_dir.directories[i];
		if(strcmp(curr_dir.dname,directory)==0 && ret==1)
		{
			fclose(diskptr);
			return 0;
		}
		
		//if looking for file or file with extension, but the subdirectory is right
		else if(strcmp(curr_dir.dname,directory)==0 &&(ret==2 || ret==3))
		{
		
			size_t index_block = curr_dir.n_start_block;
			//move pointer in .disk to spot of directory
			fseek(diskptr,index_block*BLOCK_SIZE, SEEK_SET);
			//read subdirectory block
			struct cs1550_directory_entry sub_dir;
			fread(&sub_dir,sizeof(struct cs1550_directory_entry),1,diskptr);
			
			//look for matching file name and/or matching extension name
			for(size_t j=0; j<sub_dir.num_files; j++)
			{
				struct cs1550_file_entry file = sub_dir.files[j];
				if(strcmp(file.fname,filename)==0 && ret==2)
				{
					fclose(diskptr);
					return 0;
				}
				
				else if(strcmp(file.fname,filename)==0 && strcmp(file.fext,extension)==0)
				{
					fclose(diskptr);
					return 0;
				}
			}
		}
	}
	//if we get here, messed up
	fclose(diskptr);
	return -ENOENT;

}

/**
 * Called when close is called on a file descriptor, but because it might
 * have been dup'ed, this isn't a guarantee we won't ever need the file
 * again. For us, return success simply to avoid the unimplemented error
 * in the debug log.
 */
static int cs1550_flush(const char *path, struct fuse_file_info *fi)
{
	(void) path;
	(void) fi;
	//Don't need to implement this
	// Success!
	return 0;
}

/**
 * This function should be used to open and/or initialize your `.disk` file.
 */
static void *cs1550_init(struct fuse_conn_info *fi)
{
	(void) fi;
	//open static disk file
	disk = fopen(".disk","rb+");
	return NULL;
}

/**
 * This function should be used to close the `.disk` file.
 */
static void cs1550_destroy(void *args)
{
	(void) args;
	//close static disk file
	fclose(disk);
}

/*
 * Register our new functions as the implementations of the syscalls.
 */
static struct fuse_operations cs1550_oper = {
	.getattr	= cs1550_getattr,
	.readdir	= cs1550_readdir,
	.mkdir		= cs1550_mkdir,
	.rmdir		= cs1550_rmdir,
	.read		= cs1550_read,
	.write		= cs1550_write,
	.mknod		= cs1550_mknod,
	.unlink		= cs1550_unlink,
	.truncate	= cs1550_truncate,
	.flush		= cs1550_flush,
	.open		= cs1550_open,
	.init		= cs1550_init,
	.destroy	= cs1550_destroy,
};

/*
 * Don't change this.
 */
int main(int argc, char *argv[])
{
	return fuse_main(argc, argv, &cs1550_oper, NULL);
}
