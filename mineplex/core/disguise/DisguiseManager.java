package mineplex.core.disguise;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import mineplex.core.MiniPlugin;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilMath;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseBlock;
import mineplex.core.disguise.disguises.DisguiseInsentient;
import mineplex.core.packethandler.IPacketRunnable;
import mineplex.core.packethandler.PacketArrayList;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import net.minecraft.server.v1_6_R3.ChunkAddEntityEvent;
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.Packet;
import net.minecraft.server.v1_6_R3.Packet18ArmAnimation;
import net.minecraft.server.v1_6_R3.Packet20NamedEntitySpawn;
import net.minecraft.server.v1_6_R3.Packet24MobSpawn;
import net.minecraft.server.v1_6_R3.Packet28EntityVelocity;
import net.minecraft.server.v1_6_R3.Packet29DestroyEntity;
import net.minecraft.server.v1_6_R3.Packet31RelEntityMove;
import net.minecraft.server.v1_6_R3.Packet33RelEntityMoveLook;
import net.minecraft.server.v1_6_R3.Packet34EntityTeleport;
import net.minecraft.server.v1_6_R3.Packet40EntityMetadata;
import net.minecraft.server.v1_6_R3.Packet44UpdateAttributes;
import net.minecraft.server.v1_6_R3.Packet5EntityEquipment;
import net.minecraft.server.v1_6_R3.Packet62NamedSoundEffect;
import net.minecraft.server.v1_6_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class DisguiseManager extends MiniPlugin implements IPacketRunnable
{
  private NautHashMap<Integer, DisguiseBase> _spawnPacketMap = new NautHashMap();
  private NautHashMap<Integer, Packet28EntityVelocity> _movePacketMap = new NautHashMap();
  private NautHashMap<Integer, Packet28EntityVelocity> _moveTempMap = new NautHashMap();
  private HashSet<Integer> _goingUp = new HashSet();
  private NautHashMap<String, DisguiseBase> _entityDisguiseMap = new NautHashMap();
  private NautHashMap<String, EntityType> _addTempList = new NautHashMap();
  private HashSet<String> _delTempList = new HashSet();
  
  private Field _attributesA;
  private Field _soundB;
  private Field _soundC;
  private Field _soundD;
  
  public DisguiseManager(JavaPlugin plugin, PacketHandler packetHandler)
  {
    super("Disguise Manager", plugin);
    
    packetHandler.AddPacketRunnable(this);
    
    try
    {
      this._attributesA = Packet44UpdateAttributes.class.getDeclaredField("a");
      this._attributesA.setAccessible(true);
      this._soundB = Packet62NamedSoundEffect.class.getDeclaredField("b");
      this._soundB.setAccessible(true);
      this._soundC = Packet62NamedSoundEffect.class.getDeclaredField("c");
      this._soundC.setAccessible(true);
      this._soundD = Packet62NamedSoundEffect.class.getDeclaredField("d");
      this._soundD.setAccessible(true);
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
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
  
  public boolean isDisguised(LivingEntity entity)
  {
    return this._spawnPacketMap.containsKey(Integer.valueOf(entity.getEntityId()));
  }
  
  public DisguiseBase getDisguise(LivingEntity entity)
  {
    return (DisguiseBase)this._spawnPacketMap.get(Integer.valueOf(entity.getEntityId()));
  }
  
  public void disguise(DisguiseBase disguise)
  {
    if (!disguise.GetEntity().isAlive()) {
      return;
    }
    this._spawnPacketMap.put(Integer.valueOf(disguise.GetEntityId()), disguise);
    
    reApplyDisguise(disguise);
  }
  
  public void undisguise(LivingEntity entity)
  {
    if (!this._spawnPacketMap.containsKey(Integer.valueOf(entity.getEntityId()))) {
      return;
    }
    for (Player player : Bukkit.getOnlinePlayers())
    {
      if (entity != player)
      {

        EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
        entityPlayer.playerConnection.sendPacket(new Packet29DestroyEntity(new int[] { entity.getEntityId() }));
        
        if ((entity instanceof Player))
        {
          player.showPlayer((Player)entity);
        }
        else
        {
          entityPlayer.playerConnection.sendPacket(new Packet24MobSpawn(((CraftLivingEntity)entity).getHandle()));
        }
      }
    }
    this._spawnPacketMap.remove(Integer.valueOf(entity.getEntityId()));
    this._movePacketMap.remove(Integer.valueOf(entity.getEntityId()));
    this._moveTempMap.remove(Integer.valueOf(entity.getEntityId()));
  }
  
  public void reApplyDisguise(final DisguiseBase disguise)
  {
    for (Player player : )
    {
      if (disguise.GetEntity() != ((CraftPlayer)player).getHandle())
      {

        EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
        
        entityPlayer.playerConnection.sendPacket(new Packet29DestroyEntity(new int[] { disguise.GetEntityId() }));
      }
    }
    List<Packet> tempArmor = new java.util.ArrayList();
    
    if (((disguise instanceof DisguiseInsentient)) && ((disguise.GetEntity() instanceof LivingEntity)))
    {
      if (((DisguiseInsentient)disguise).armorVisible())
      {
        for (Packet armorPacket : ((DisguiseInsentient)disguise).getArmorPackets()) {
          tempArmor.add(armorPacket);
        }
      }
    }
    final List<Packet> armorPackets = tempArmor;
    
    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GetPlugin(), new Runnable()
    {
      public void run()
      {
        for (Player player : )
        {
          if (disguise.GetEntity() != ((CraftPlayer)player).getHandle())
          {

            EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
            entityPlayer.playerConnection.sendPacket(disguise.GetSpawnPacket());
            
            for (Packet packet : armorPackets)
            {
              entityPlayer.playerConnection.sendPacket(packet);
            }
          }
        }
      }
    });
  }
  
  public void updateDisguise(DisguiseBase disguise) {
    for (Player player : )
    {
      if (disguise.GetEntity() != ((CraftPlayer)player).getHandle())
      {

        EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
        
        entityPlayer.playerConnection.sendPacket(disguise.GetMetaDataPacket());
      }
    }
  }
  
  @EventHandler
  public void ChunkUnload(ChunkUnloadEvent event) {
    for (org.bukkit.entity.Entity entity : event.getChunk().getEntities())
    {
      if (this._spawnPacketMap.containsKey(Integer.valueOf(entity.getEntityId())))
      {
        this._entityDisguiseMap.put(entity.getUniqueId().toString(), (DisguiseBase)this._spawnPacketMap.get(Integer.valueOf(entity.getEntityId())));
        this._spawnPacketMap.remove(Integer.valueOf(entity.getEntityId()));
      }
    }
  }
  
  @EventHandler
  public void ChunkAddEntity(ChunkAddEntityEvent event)
  {
    DisguiseBase disguise = (DisguiseBase)this._entityDisguiseMap.get(event.GetEntity().getUniqueId().toString());
    
    if (disguise != null)
    {
      disguise.UpdateEntity(((CraftLivingEntity)event.GetEntity()).getHandle());
      this._spawnPacketMap.put(Integer.valueOf(event.GetEntity().getEntityId()), disguise);
      this._entityDisguiseMap.remove(event.GetEntity().getUniqueId().toString());
    }
  }
  
  @EventHandler
  public void TeleportDisguises(UpdateEvent event)
  {
    if (event.getType() != UpdateType.SEC) {
      return;
    }
    for (Player player : Bukkit.getOnlinePlayers())
    {
      for (Player otherPlayer : Bukkit.getOnlinePlayers())
      {
        if (player != otherPlayer)
        {

          if (otherPlayer.getLocation().subtract(0.0D, 0.5D, 0.0D).getBlock().getTypeId() != 0)
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new Packet34EntityTeleport(((CraftPlayer)otherPlayer).getHandle()));
        }
      }
    }
  }
  
  public void clearDisguises() {
    this._spawnPacketMap.clear();
    this._movePacketMap.clear();
    this._moveTempMap.clear();
    this._goingUp.clear();
    this._entityDisguiseMap.clear();
    this._addTempList.clear();
    this._delTempList.clear();
  }
  
  @EventHandler
  public void PlayerQuit(PlayerQuitEvent event)
  {
    undisguise(event.getPlayer());
  }
  

  public boolean run(Packet packet, Player owner, final PacketArrayList packetList)
  {
    if ((packet instanceof Packet20NamedEntitySpawn))
    {
      int entityId = ((Packet20NamedEntitySpawn)packet).a;
      
      if (this._spawnPacketMap.containsKey(Integer.valueOf(entityId)))
      {
        packetList.forceAdd(((DisguiseBase)this._spawnPacketMap.get(Integer.valueOf(entityId))).GetSpawnPacket());
        return false;
      }
    }
    else if ((packet instanceof Packet24MobSpawn))
    {
      int entityId = ((Packet24MobSpawn)packet).a;
      
      if (this._spawnPacketMap.containsKey(Integer.valueOf(entityId)))
      {
        packetList.forceAdd(((DisguiseBase)this._spawnPacketMap.get(Integer.valueOf(entityId))).GetSpawnPacket());
        return false;
      }
    }
    else if ((packet instanceof Packet44UpdateAttributes))
    {
      int entityId = -1;
      
      try
      {
        entityId = ((Integer)this._attributesA.get((Packet44UpdateAttributes)packet)).intValue();
      }
      catch (IllegalArgumentException e)
      {
        e.printStackTrace();
      }
      catch (IllegalAccessException e)
      {
        e.printStackTrace();
      }
      
      if ((this._spawnPacketMap.containsKey(Integer.valueOf(entityId))) && (owner.getEntityId() != entityId))
      {

        if ((this._spawnPacketMap.get(Integer.valueOf(entityId)) instanceof DisguiseBlock)) {
          return false;
        }
      }
    } else if ((packet instanceof Packet18ArmAnimation))
    {
      int entityId = ((Packet18ArmAnimation)packet).a;
      
      if ((this._spawnPacketMap.containsKey(Integer.valueOf(entityId))) && (owner.getEntityId() != entityId))
      {
        return false;
      }
    }
    else if ((packet instanceof Packet40EntityMetadata))
    {
      int entityId = ((Packet40EntityMetadata)packet).a;
      
      if ((this._spawnPacketMap.containsKey(Integer.valueOf(entityId))) && (owner.getEntityId() != entityId))
      {
        packetList.forceAdd(((DisguiseBase)this._spawnPacketMap.get(Integer.valueOf(entityId))).GetMetaDataPacket());
        return false;
      }
    }
    else if ((packet instanceof Packet5EntityEquipment))
    {
      int entityId = ((Packet5EntityEquipment)packet).a;
      
      if ((this._spawnPacketMap.containsKey(Integer.valueOf(entityId))) && ((this._spawnPacketMap.get(Integer.valueOf(entityId)) instanceof DisguiseInsentient)))
      {
        if ((!((DisguiseInsentient)this._spawnPacketMap.get(Integer.valueOf(entityId))).armorVisible()) && (((Packet5EntityEquipment)packet).b != 0))
        {
          return false;
        }
      }
    }
    else if ((packet instanceof Packet28EntityVelocity))
    {
      Packet28EntityVelocity velocityPacket = (Packet28EntityVelocity)packet;
      

      if (velocityPacket.a == owner.getEntityId())
      {
        if (velocityPacket.c > 0)
          this._goingUp.add(Integer.valueOf(velocityPacket.a));
      } else {
        if ((velocityPacket.b == 0) && (velocityPacket.c == 0) && (velocityPacket.d == 0))
        {
          return true;
        }
        if (this._spawnPacketMap.containsKey(Integer.valueOf(velocityPacket.a)))
        {
          return false;
        }
      }
    } else if ((packet instanceof Packet31RelEntityMove))
    {
      Packet31RelEntityMove movePacket = (Packet31RelEntityMove)packet;
      

      if (movePacket.a == owner.getEntityId()) {
        return true;
      }
      if ((this._goingUp.contains(Integer.valueOf(movePacket.a))) && (movePacket.c != 0) && (movePacket.c < 20))
      {
        this._goingUp.remove(Integer.valueOf(movePacket.a));
        this._movePacketMap.remove(Integer.valueOf(movePacket.a));
      }
      
      if (!this._spawnPacketMap.containsKey(Integer.valueOf(movePacket.a))) {
        return true;
      }
      final Packet28EntityVelocity velocityPacket = new Packet28EntityVelocity();
      velocityPacket.a = movePacket.a;
      velocityPacket.b = (movePacket.b * 100);
      velocityPacket.c = (movePacket.c * 100);
      velocityPacket.d = (movePacket.d * 100);
      
      if (this._movePacketMap.containsKey(Integer.valueOf(movePacket.a)))
      {
        Packet28EntityVelocity lastVelocityPacket = (Packet28EntityVelocity)this._movePacketMap.get(Integer.valueOf(movePacket.a));
        
        velocityPacket.b = ((int)(0.8D * lastVelocityPacket.b));
        velocityPacket.c = ((int)(0.8D * lastVelocityPacket.c));
        velocityPacket.d = ((int)(0.8D * lastVelocityPacket.d));
      }
      
      this._movePacketMap.put(Integer.valueOf(movePacket.a), velocityPacket);
      
      packetList.forceAdd(velocityPacket);
      
      if ((this._goingUp.contains(Integer.valueOf(movePacket.a))) && (movePacket.c != 0) && (movePacket.c > 20))
      {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GetPlugin(), new Runnable()
        {
          public void run()
          {
            packetList.forceAdd(velocityPacket);
          }
        });
      }
      
      (this._spawnPacketMap.get(Integer.valueOf(movePacket.a)) instanceof DisguiseBlock);



    }
    else if ((packet instanceof Packet33RelEntityMoveLook))
    {
      Packet33RelEntityMoveLook movePacket = (Packet33RelEntityMoveLook)packet;
      

      if (movePacket.a == owner.getEntityId()) {
        return true;
      }
      if ((this._goingUp.contains(Integer.valueOf(movePacket.a))) && (movePacket.c != 0) && (movePacket.c <= 20))
      {
        this._goingUp.remove(Integer.valueOf(movePacket.a));
        this._movePacketMap.remove(Integer.valueOf(movePacket.a));
      }
      
      if (!this._spawnPacketMap.containsKey(Integer.valueOf(movePacket.a))) {
        return true;
      }
      final Packet28EntityVelocity velocityPacket = new Packet28EntityVelocity();
      velocityPacket.a = movePacket.a;
      velocityPacket.b = (movePacket.b * 100);
      velocityPacket.c = (movePacket.c * 100);
      velocityPacket.d = (movePacket.d * 100);
      
      if (this._movePacketMap.containsKey(Integer.valueOf(movePacket.a)))
      {
        Packet28EntityVelocity lastVelocityPacket = (Packet28EntityVelocity)this._movePacketMap.get(Integer.valueOf(movePacket.a));
        
        velocityPacket.b = ((int)(0.8D * lastVelocityPacket.b));
        velocityPacket.c = ((int)(0.8D * lastVelocityPacket.c));
        velocityPacket.d = ((int)(0.8D * lastVelocityPacket.d));
      }
      
      this._movePacketMap.put(Integer.valueOf(movePacket.a), velocityPacket);
      
      packetList.forceAdd(velocityPacket);
      
      if ((this._goingUp.contains(Integer.valueOf(movePacket.a))) && (movePacket.c != 0) && (movePacket.c > 20))
      {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GetPlugin(), new Runnable()
        {
          public void run()
          {
            packetList.forceAdd(velocityPacket);
          }
        });
      }
    }
    else if ((packet instanceof Packet62NamedSoundEffect))
    {
      try
      {
        int x = ((Integer)this._soundB.get((Packet62NamedSoundEffect)packet)).intValue() / 8;
        int y = ((Integer)this._soundC.get((Packet62NamedSoundEffect)packet)).intValue() / 8;
        int z = ((Integer)this._soundD.get((Packet62NamedSoundEffect)packet)).intValue() / 8;
        
        for (DisguiseBase disguise : this._spawnPacketMap.values())
        {
          if ((disguise instanceof mineplex.core.disguise.disguises.DisguisePlayer))
          {

            if (UtilMath.offset(new Vector(disguise.GetEntity().locX, disguise.GetEntity().locY, disguise.GetEntity().locZ), new Vector(x, y, z)) <= 2.0D)
            {
              return false;
            }
          }
        }
      }
      catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    
    return true;
  }
  
  public void PrepAddDisguise(Player caller, EntityType entityType)
  {
    this._addTempList.put(caller.getName(), entityType);
  }
  
  public void PrepRemoveDisguise(Player caller)
  {
    this._delTempList.add(caller.getName());
  }
}
