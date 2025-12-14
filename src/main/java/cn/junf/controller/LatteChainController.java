package cn.junf.controller;

import cn.junf.JNA.AuthLibrary;
import cn.junf.JNA.AuthServer;
import cn.junf.crypto.JPBCRSAAccumulator;
import cn.junf.service.*;
import cn.junf.utils.JsonUtil;
import it.unisa.dia.gas.jpbc.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;


@Controller
public class LatteChainController {
    @Autowired
    private IUserService userService;

    @Autowired
    private IChainService chainService;

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private IThresholdService thresholdService;

    @Autowired
    private IVoteService voteService;

    @Autowired
    private IChameleonService chameleonService;

    /**
     * 初始化LatteChain区块链系统，初始化预置账户并创建创世块
     *
     * @param model
     * @return
     */
    @GetMapping("/init")
    public String initSystem(Model model) {
        long startTime = System.currentTimeMillis();    //获取开始时间

        if (chainService.initchain()) {
            long endTime = System.currentTimeMillis();  //获取结束时间
            System.out.println("初始化总耗时：" + (endTime - startTime) + "ms"); //输出程序运行时间
            return "init";
        } else {
            model.addAttribute("msg", "初始化失败，已初始化或系统错误！");
            long endTime = System.currentTimeMillis();
            System.out.println("初始化失败时间：" + (endTime - startTime) + "ms");
            return "error";
        }
    }

    /**
     * 查看当前所有的账户信息
     */
    @GetMapping("/allUsersInfo")
    public String getAllUsersInfo(Model model) {
        model.addAttribute("usersInfo", userService.getAllUsersInfo());
        return "allUsers";
    }

    @GetMapping("/getUserInfo/{address}")
    @ResponseBody
    public String getUserInfo(@PathVariable String address) {
        return userService.getUserPublicKey(address).toString();
    }

    /**
     * 交易发起接口
     *
     * @param sender    发起方账户地址
     * @param recipient 接收方账户地址
     * @param value     交易金额
     * @param model
     * @return String
     */
    @PostMapping(path = "trade")
    public String sendFuns(@RequestParam(name = "sender") String sender,
                           @RequestParam(name = "recipient") String recipient,
                           @RequestParam(name = "value") float value,
                           @RequestParam(name = "msg") String msg,
                           Model model) {
        long startTime = System.currentTimeMillis();//获取开始时间

        model.addAttribute("transactionInfo",
                transactionService.createTransaction(sender, recipient, value, msg));

        long endTime = System.currentTimeMillis();//获取结束时间
        System.out.println("交易总耗时：" + (endTime - startTime) + "ms");

        return "transaction";

    }

    /**
     * @param id 交易ID
     * @return 交易信息
     */
    @GetMapping(path = "/getT")
    @ResponseBody
    public String getTransaction(@RequestParam(name = "transactionId") String id) {
        return transactionService.getTransaction(id);
    }

    /**
     * 提出 修改交易请求
     *
     * @param id     交易ID
     * @param sender 发起方地址
     * @return
     */
    @PostMapping(path = "addModifyRequest")
    @ResponseBody
    public String addModifyRequest(@RequestParam(name = "transactionId") String id,
                                   @RequestParam(name = "userName") String sender
    ) {
        if (thresholdService.modifyRequest(sender, id)) {
            return JsonUtil.toJson("提出 修改交易请求 成功");
        }
        return JsonUtil.toJson("请求 失败");
    }

    /**
     * 对交易请求进行投票
     *
     * @param id
     * @param sender
     */
    @PostMapping(path = "voteModifyRequest")
    @ResponseBody
    public String voteModifyRequest(@RequestParam(name = "transactionId") String id,
                                    @RequestParam(name = "userName") String sender) {
        if (voteService.vote(sender, id)) {
            return JsonUtil.toJson("投票成功");
        }
        return JsonUtil.toJson("投票失败");
    }

    /**
     * 修改交易信息
     *
     * @param id
     * @param name
     * @param newMsg
     */
    @PostMapping(path = "modify")
    @ResponseBody
    public String modifyTransaction(@RequestParam(name = "transactionId") String id,
                                    @RequestParam(name = "userName") String name,
                                    @RequestParam(name = "newMsg") String newMsg
    ) throws Exception {
//        transactionService.modifyTransaction(name, id, newMsg);
//        return JsonUtil.toJson("修改成功");
        if (voteService.voteVerify(id)) {
            transactionService.modifyTransaction(name, id, newMsg);
            return JsonUtil.toJson("修改成功");
        } else return JsonUtil.toJson("未收集到足够的秘钥份额");
    }

    /**
     * 生成成员证明
     *
     * @param newMsg 待验证消息
     * @return 成员证明
     */
    @PostMapping(path = "verifyMemCerRSA")
    @ResponseBody
    public Boolean generateMemCer(@RequestParam(name = "newMsg") String newMsg) {
        Element element = chameleonService.generateMemCer((newMsg));
        byte[] bytes = element.toBytes();
        return chameleonService.verifyMemCer(bytes,newMsg);
    }


    @PostMapping(path = "/test")
    public void test() {
        System.out.println(AuthLibrary.INSTANCE.add(1, 2));
        int count = 1;
        for (int i = 0; i < count; i++) {
            String msg = "测试 java -> go,java -> go,java -> go,java -> go,java -> go,java -> go,java -> go,java -> go";
            System.out.println(AuthServer.hello(msg));
        }
        count = 999999999;
    }

}
