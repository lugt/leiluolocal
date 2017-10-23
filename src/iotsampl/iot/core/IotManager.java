package iotsampl.iot.core;

import iotsampl.iot.data.IotSync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Frapo on 2017/8/8.
 * Version :15
 * Earth - Moudule Radnor.Utils
 */
public class IotManager {

    public static boolean exist = false;
    public static IotManager me;

    public static IotManager getOne(){
        if(me == null) {
            exist = false;
            me = new IotManager();
        }
        return me;
    }

    public IotManager(){
        // 初始化 Sync 对象
        if(exist) return;
        exist = true;
        IotSync.init();
    }


    private Map<Integer,IotNode> nodes = new HashMap<>();

    private Map<Integer,IotNode> gnodes = new HashMap<>();

    public IotNode getNode(int i) {
        if(gnodes.containsKey(i)){
            return gnodes.get(i);
        }else{
            IotNode node = new IotNode(i);
            node.init();
            gnodes.put(i, node);
            return node;
        }
    }

    public List<Integer> getHourList() {
        ArrayList<Integer> list = new ArrayList<>(7);
        list.add(30304009);
        list.add(30304010);
        list.add(30304011);
        list.add(40000001);
        list.add(40000002);
        list.add(40100001);
        list.add(40100002);
        list.add(40200001);
        list.add(40200002);
        return list;
    }

    public List<Integer> getClawList() {
        ArrayList<Integer> list = new ArrayList<>(10);
        list.add(30304009);
        list.add(30304010);
        list.add(30304011);
        list.add(40000001);
        list.add(40000002);
        list.add(40100001);
        list.add(40100002);
        list.add(40200001);
        list.add(40200002);
        return list;
    }

    public List<Integer> getAllHexNodes() {
        List<Integer> list = new ArrayList<>(7);
        list.add(30304009);
        list.add(40000001);
        list.add(40000002);
        list.add(40100001);
        list.add(40100002);
        list.add(40200001);
        list.add(40200002);

        return list;
    }
    // IotManager
}
