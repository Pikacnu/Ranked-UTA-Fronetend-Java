package com.pikacnu.src;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.command.argument.NbtPathArgumentType;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * Storage NBT 數據獲取工具類
 * 提供簡單的 API 來獲取和操作 Minecraft 的 Storage NBT 數據
 */
public class StorageNbtHelper {

  /**
   * 獲取指定 Storage 的完整 NBT 數據
   * 
   * @param server    Minecraft 服務器實例
   * @param storageId Storage 的識別符 (例如: "minecraft:my_data")
   * @return 完整的 NBT 數據，如果不存在則返回空的 NbtCompound
   */
  public static NbtCompound getStorageNbt(MinecraftServer server, String storageId) {
    Identifier identifier = Identifier.tryParse(storageId);
    if (identifier == null) {
      throw new IllegalArgumentException("無效的 Storage ID: " + storageId);
    }
    return server.getDataCommandStorage().get(identifier);
  }

  /**
   * 獲取指定 Storage 中特定路徑的 NBT 數據
   * 
   * @param server    Minecraft 服務器實例
   * @param storageId Storage 的識別符
   * @param path      NBT 路徑 (例如: "player.health")
   * @return 路徑對應的 NBT 元素列表
   * @throws Exception 如果路徑無效或不存在
   */
  public static List<NbtElement> getStorageNbtByPath(MinecraftServer server, String storageId, String path)
      throws Exception {
    NbtCompound storageNbt = getStorageNbt(server, storageId);

    if (storageNbt.isEmpty()) {
      return new ArrayList<>();
    }

    try {
      NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.nbtPath()
          .parse(new com.mojang.brigadier.StringReader(path));
      Collection<NbtElement> elements = nbtPath.get(storageNbt);
      return new ArrayList<>(elements);
    } catch (Exception e) {
      throw new Exception("無法解析 NBT 路徑: " + path, e);
    }
  }

  /**
   * 設置指定 Storage 的 NBT 數據
   * 
   * @param server    Minecraft 服務器實例
   * @param storageId Storage 的識別符
   * @param nbt       要設置的 NBT 數據
   */
  public static void setStorageNbt(MinecraftServer server, String storageId, NbtCompound nbt) {
    Identifier identifier = Identifier.tryParse(storageId);
    if (identifier == null) {
      throw new IllegalArgumentException("無效的 Storage ID: " + storageId);
    }
    server.getDataCommandStorage().set(identifier, nbt);
  }

  /**
   * 在指定 Storage 的特定路徑設置 NBT 數據
   * 
   * @param server    Minecraft 服務器實例
   * @param storageId Storage 的識別符
   * @param path      NBT 路徑
   * @param value     要設置的 NBT 元素
   * @throws Exception 如果操作失敗
   */
  public static void setStorageNbtByPath(MinecraftServer server, String storageId, String path, NbtElement value)
      throws Exception {
    NbtCompound storageNbt = getStorageNbt(server, storageId);

    try {
      NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.nbtPath()
          .parse(new com.mojang.brigadier.StringReader(path));
      nbtPath.put(storageNbt, value);
      setStorageNbt(server, storageId, storageNbt);
    } catch (Exception e) {
      throw new Exception("無法設置 NBT 路徑: " + path, e);
    }
  }

  /**
   * 合併 NBT 數據到指定 Storage
   * 
   * @param server    Minecraft 服務器實例
   * @param storageId Storage 的識別符
   * @param mergeNbt  要合併的 NBT 數據
   */
  public static void mergeStorageNbt(MinecraftServer server, String storageId, NbtCompound mergeNbt) {
    NbtCompound existingNbt = getStorageNbt(server, storageId);
    NbtCompound resultNbt = existingNbt.copy();
    resultNbt.copyFrom(mergeNbt);
    setStorageNbt(server, storageId, resultNbt);
  }

  /**
   * 移除指定 Storage 中特定路徑的數據
   * 
   * @param server    Minecraft 服務器實例
   * @param storageId Storage 的識別符
   * @param path      要移除的 NBT 路徑
   * @return 移除的元素數量
   * @throws Exception 如果操作失敗
   */
  public static int removeStorageNbtByPath(MinecraftServer server, String storageId, String path) throws Exception {
    NbtCompound storageNbt = getStorageNbt(server, storageId);

    try {
      NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.nbtPath()
          .parse(new com.mojang.brigadier.StringReader(path));
      int removedCount = nbtPath.remove(storageNbt);
      if (removedCount > 0) {
        setStorageNbt(server, storageId, storageNbt);
      }
      return removedCount;
    } catch (Exception e) {
      throw new Exception("無法移除 NBT 路徑: " + path, e);
    }
  }

  /**
   * 檢查指定 Storage 是否存在
   * 
   * @param server    Minecraft 服務器實例
   * @param storageId Storage 的識別符
   * @return 如果存在且不為空則返回 true
   */
  public static boolean hasStorageData(MinecraftServer server, String storageId) {
    NbtCompound nbt = getStorageNbt(server, storageId);
    return nbt != null && !nbt.isEmpty();
  }

  /**
   * 清空指定 Storage 的所有數據
   * 
   * @param server    Minecraft 服務器實例
   * @param storageId Storage 的識別符
   */
  public static void clearStorage(MinecraftServer server, String storageId) {
    setStorageNbt(server, storageId, new NbtCompound());
  }
}
