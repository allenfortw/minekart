package mineplex.core.fakeEntity;

import java.lang.reflect.Field;
import net.minecraft.server.v1_6_R3.BlockCloth;
import net.minecraft.server.v1_6_R3.DataWatcher;
import net.minecraft.server.v1_6_R3.EnumEntitySize;
import net.minecraft.server.v1_6_R3.MathHelper;
import net.minecraft.server.v1_6_R3.Packet;
import net.minecraft.server.v1_6_R3.Packet24MobSpawn;
import net.minecraft.server.v1_6_R3.Packet29DestroyEntity;
import net.minecraft.server.v1_6_R3.Packet39AttachEntity;
import net.minecraft.server.v1_6_R3.Packet40EntityMetadata;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;



public class FakeEntity
{
  private static int _entityIdCounter = 50000;
  
  private int _entityId;
  
  private Location _location;
  private EntityType _entityType;
  private static Field _spawnDataWatcherField;
  private static Field _spawnListField;
  
  public FakeEntity(EntityType entityType, Location location)
  {
    this._entityId = (_entityIdCounter++);
    this._entityType = entityType;
    this._location = location;
    
    SetSpawnDataWatcherField();
    SetSpawnListField();
  }
  
  public int GetEntityId()
  {
    return this._entityId;
  }
  
  public EntityType GetEntityType()
  {
    return this._entityType;
  }
  
  public void SetLocation(Location location)
  {
    this._location = location;
  }
  
  public Location GetLocation()
  {
    return this._location;
  }
  
  public Packet Spawn()
  {
    return Spawn(this._entityId);
  }
  
  public Packet Destroy()
  {
    Packet29DestroyEntity packet = new Packet29DestroyEntity();
    packet.a = new int[] { GetEntityId() };
    
    return packet;
  }
  
  public Packet Spawn(int id)
  {
    Packet24MobSpawn packet = new Packet24MobSpawn();
    packet.a = id;
    packet.b = ((byte)this._entityType.getTypeId());
    packet.c = EnumEntitySize.SIZE_2.a(this._location.getX());
    packet.d = MathHelper.floor(this._location.getY() * 32.0D);
    packet.e = EnumEntitySize.SIZE_2.a(this._location.getZ());
    packet.i = ((byte)(int)(this._location.getYaw() * 256.0F / 360.0F));
    packet.j = ((byte)(int)(this._location.getPitch() * 256.0F / 360.0F));
    packet.k = ((byte)(int)(this._location.getYaw() * 256.0F / 360.0F));
    
    double var2 = 3.9D;
    double var4 = 0.0D;
    double var6 = 0.0D;
    double var8 = 0.0D;
    
    if (var4 < -var2)
    {
      var4 = -var2;
    }
    
    if (var6 < -var2)
    {
      var6 = -var2;
    }
    
    if (var8 < -var2)
    {
      var8 = -var2;
    }
    
    if (var4 > var2)
    {
      var4 = var2;
    }
    
    if (var6 > var2)
    {
      var6 = var2;
    }
    
    if (var8 > var2)
    {
      var8 = var2;
    }
    
    packet.f = ((int)(var4 * 8000.0D));
    packet.g = ((int)(var6 * 8000.0D));
    packet.h = ((int)(var8 * 8000.0D));
    
    DataWatcher dataWatcher = new DataWatcher();
    
    UpdateDataWatcher(dataWatcher);
    
    try
    {
      _spawnDataWatcherField.set(packet, dataWatcher);
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }
    
    try
    {
      _spawnListField.set(packet, dataWatcher.b());
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }
    
    return packet;
  }
  
  public Packet Hide()
  {
    return Hide(this._entityId);
  }
  
  public Packet Hide(int entityId)
  {
    DataWatcher dataWatcher = new DataWatcher();
    
    UpdateDataWatcher(dataWatcher);
    
    dataWatcher.watch(0, Byte.valueOf((byte)32));
    
    return new Packet40EntityMetadata(entityId, dataWatcher, false);
  }
  
  public Packet Show()
  {
    DataWatcher dataWatcher = new DataWatcher();
    
    UpdateDataWatcher(dataWatcher);
    
    return new Packet40EntityMetadata(this._entityId, dataWatcher, true);
  }
  
  public Packet SetVehicle(int vehicleId)
  {
    Packet39AttachEntity vehiclePacket = new Packet39AttachEntity();
    vehiclePacket.a = 0;
    vehiclePacket.b = this._entityId;
    vehiclePacket.c = vehicleId;
    
    return vehiclePacket;
  }
  
  public Packet SetPassenger(int passengerId)
  {
    return SetPassenger(passengerId, this._entityId);
  }
  
  public Packet SetPassenger(int passengerId, int entityId)
  {
    Packet39AttachEntity vehiclePacket = new Packet39AttachEntity();
    vehiclePacket.a = 0;
    vehiclePacket.b = passengerId;
    vehiclePacket.c = entityId;
    
    return vehiclePacket;
  }
  
  protected void UpdateDataWatcher(DataWatcher dataWatcher)
  {
    dataWatcher.a(0, Byte.valueOf((byte)0));
    dataWatcher.a(1, Short.valueOf((short)300));
    dataWatcher.a(6, Float.valueOf(4.0F));
    dataWatcher.a(7, Integer.valueOf(0));
    dataWatcher.a(8, Byte.valueOf((byte)0));
    dataWatcher.a(9, Byte.valueOf((byte)0));
    dataWatcher.a(10, " ");
    dataWatcher.a(11, Byte.valueOf((byte)0));
    
    switch (this._entityType)
    {
    case MINECART: 
    case MINECART_TNT: 
    case PIG: 
    case PLAYER: 
    case PRIMED_TNT: 
    case SPIDER: 
      dataWatcher.a(16, new Byte((byte)0));
      break;
    case HORSE: 
      dataWatcher.a(16, Byte.valueOf((byte)-1));
      dataWatcher.a(17, Byte.valueOf((byte)0));
      break;
    case MINECART_FURNACE: 
      dataWatcher.a(16, new Byte((byte)0));
      dataWatcher.a(17, new Byte((byte)0));
      dataWatcher.a(18, new Byte((byte)0));
      break;
    case WITHER_SKULL: 
      dataWatcher.a(16, Byte.valueOf((byte)0));
      dataWatcher.a(17, Byte.valueOf((byte)0));
      break;
    case ITEM_FRAME: 
    case SNOWMAN: 
      dataWatcher.a(16, new Byte((byte)0));
      break;
    case SLIME: 
      dataWatcher.a(18, new Integer(20));
      dataWatcher.a(19, new Byte((byte)0));
      dataWatcher.a(20, new Byte((byte)BlockCloth.j_(1)));
    case IRON_GOLEM: 
      dataWatcher.a(13, new Byte((byte)0));
      break;
    case MAGMA_CUBE: 
      dataWatcher.a(16, new Byte((byte)1));
      break;
    case PIG_ZOMBIE: 
      dataWatcher.a(21, Byte.valueOf((byte)0));
      break;
    case PAINTING: 
      dataWatcher.a(16, new Integer(100));
      dataWatcher.a(17, new Integer(0));
      dataWatcher.a(18, new Integer(0));
      dataWatcher.a(19, new Integer(0));
      dataWatcher.a(20, new Integer(0));
      break;
    case LIGHTNING: 
      dataWatcher.a(13, Byte.valueOf((byte)0));
      dataWatcher.a(14, Byte.valueOf((byte)0));
      break;
    }
    
  }
  


  private void SetSpawnListField()
  {
    if (_spawnListField == null)
    {
      try
      {
        _spawnListField = Packet24MobSpawn.class.getDeclaredField("u");
        _spawnListField.setAccessible(true);

      }
      catch (NoSuchFieldException e)
      {
        e.printStackTrace();

      }
      catch (SecurityException e)
      {
        e.printStackTrace();
      }
    }
  }
  
  private void SetSpawnDataWatcherField()
  {
    if (_spawnDataWatcherField == null)
    {
      try
      {
        _spawnDataWatcherField = Packet24MobSpawn.class.getDeclaredField("t");
        _spawnDataWatcherField.setAccessible(true);

      }
      catch (NoSuchFieldException e)
      {
        e.printStackTrace();

      }
      catch (SecurityException e)
      {
        e.printStackTrace();
      }
    }
  }
}
