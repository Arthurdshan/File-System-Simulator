package command;

import java.io.IOException;
import java.util.Arrays;

import cruzapi.Block;
import cruzapi.DirEntry;
import cruzapi.Disk;
import cruzapi.Inode;
import cruzapi.Main;

public class Cp extends Command
{
	public Cp(String name)
	{
		super(name);
	}
	
	@Override
	public void execute(String[] args)
	{
		try
		{
			if(args.length == 2)
			{
				Disk disk = Main.getDisk();
				
				Inode arg0 = args[0].startsWith("/") ? new Inode(1, true) : disk.getCurrentInode();
				Inode arg1 = args[1].startsWith("/") ? new Inode(1, true) : disk.getCurrentInode();
				
				DirEntry entry0 = disk.getEntryByPath(new Block(arg0.pointer()[0], true).getEntry(0), Arrays.stream(args[0].split("/")).filter(x -> !x.isEmpty()).toArray(String[]::new), 0);
				DirEntry entry1 = disk.getEntryByPath(new Block(arg1.pointer()[0], true).getEntry(0), Arrays.stream(args[1].split("/")).filter(x -> !x.isEmpty()).toArray(String[]::new), 0);
				
				if(entry0 == null || entry1 == null)
				{
					System.out.println("File not found.");
					return;
				}
				
				Inode inode1 = new Inode(entry1.getIndex(), true);
				
				for(int i = 0; i < inode1.pointer().length; i++)
				{
					Block b = new Block(inode1.pointer()[i]);
					
					if(b.index() == 0)
					{
						b = disk.getEmptyBlock();
					}
					else
					{
						b.readFully();
					}
					
					if(b.addEntry(entry0))
					{
						inode1.pointer()[i] = b.index();
						inode1.rw();
						b.rw();
						b.setInUse(true);
						System.out.println(String.format("File \"%s\" copied to directory \"%s\".", args[0], args[1]));
						return;
					}
				}
				
				System.out.println("Inode is full.");
			}
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}