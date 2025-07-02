package com.uta2.helpers;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class StorageNbtHelper {
    private static MinecraftServer server;

    public static void setServer(MinecraftServer minecraftServer) {
        server = minecraftServer;
    }

    /**
     * 從指定的 storage 和 NBT 路徑讀取數據
     * @param storage 儲存命名空間 (e.g., "uta2:game_data")
     * @param nbtPath NBT 路徑 (e.g., "Items[0].id")
     * @return NBT 數據的字串表示，如果找不到則返回 null
     */
    public static String readNbtData(String storage, String nbtPath) {
        if (server == null) {
            System.err.println("[UTA2] Server not set in StorageNbtHelper");
            return null;
        }

        try {
            Identifier storageId = Identifier.tryParse(storage);
            if (storageId == null) {
                System.err.println("[UTA2] Invalid storage identifier: " + storage);
                return null;
            }
            
            NbtCompound nbtData = server.getDataCommandStorage().get(storageId);
            
            if (nbtData == null) {
                System.err.println("[UTA2] No data found for storage: " + storage);
                return null;
            }

            // Parse NBT path and retrieve value
            NbtElement element = getNbtElementByPath(nbtData, nbtPath);
            
            if (element == null) {
                System.err.println("[UTA2] No data found at path: " + nbtPath);
                return null;
            }

            return element.asString().orElse("");
        } catch (Exception e) {
            System.err.println("[UTA2] Error reading NBT data: " + e.getMessage());
            return null;
        }
    }

    /**
     * 獲取完整的 storage NBT 數據
     * @param storage 儲存命名空間
     * @return NBT 數據的字串表示
     */
    public static String getFullStorageData(String storage) {
        if (server == null) {
            System.err.println("[UTA2] Server not set in StorageNbtHelper");
            return null;
        }

        try {
            Identifier storageId = Identifier.tryParse(storage);
            if (storageId == null) {
                System.err.println("[UTA2] Invalid storage identifier: " + storage);
                return null;
            }
            
            NbtCompound nbtData = server.getDataCommandStorage().get(storageId);
            
            if (nbtData == null) {
                return "{}";
            }

            return nbtData.toString();
        } catch (Exception e) {
            System.err.println("[UTA2] Error reading full storage data: " + e.getMessage());
            return null;
        }
    }

    /**
     * 根據路徑從 NBT compound 中獲取元素
     * 支援簡單的路徑解析，如 "Items[0].id"
     */
    private static NbtElement getNbtElementByPath(NbtCompound nbt, String path) {
        String[] parts = path.split("\\.");
        NbtElement current = nbt;

        for (String part : parts) {
            if (current == null) return null;

            // Handle array notation like "Items[0]"
            if (part.contains("[") && part.contains("]")) {
                String key = part.substring(0, part.indexOf("["));
                String indexStr = part.substring(part.indexOf("[") + 1, part.indexOf("]"));
                
                try {
                    int index = Integer.parseInt(indexStr);
                    
                    if (current instanceof NbtCompound compound) {
                        if (compound.contains(key)) {
                            var listOpt = compound.getList(key);
                            if (listOpt.isPresent()) {
                                var list = listOpt.get();
                                if (index >= 0 && index < list.size()) {
                                    var compoundOpt = list.getCompound(index);
                                    if (compoundOpt.isPresent()) {
                                        current = compoundOpt.get();
                                    } else {
                                        return null;
                                    }
                                } else {
                                    return null;
                                }
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                // Simple key access
                if (current instanceof NbtCompound compound) {
                    if (compound.contains(part)) {
                        current = compound.get(part);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }

        return current;
    }
}
