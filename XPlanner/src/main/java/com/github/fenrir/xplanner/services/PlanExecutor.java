package com.github.fenrir.xplanner.services;

import com.github.fenrir.xcommon.actions.Action;
import com.github.fenrir.xplanner.objects.Plan;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class PlanExecutor {

    public enum State {
        OTHER_PLAN_IS_RUNNING,
        RUNNING
    }

    private static final ThreadPoolExecutor threadExecutor =
            new ThreadPoolExecutor(
                    1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
            );

    @Getter private final Map<Plan, Map<Action<Object>, AgentService.AgentExecuteResult>> executedPlansMap =
            new ConcurrentHashMap<>();

    @Getter @Setter private AgentService agentService;

    private static class Executor implements Runnable {

        @Getter @Setter private Plan plan;
        @Getter @Setter private AgentService agentService;
        @Getter @Setter private Map<Plan, Map<Action<Object>, AgentService.AgentExecuteResult>> executedPlansMap;

        public Executor(Plan plan,
                        AgentService agentService,
                        Map<Plan, Map<Action<Object>, AgentService.AgentExecuteResult>> executedPlansMap){
            this.setPlan(plan);
            this.setAgentService(agentService);
            this.setExecutedPlansMap(executedPlansMap);
        }

        @Override
        public void run() {
            Map<String, Action<Object>> actionMap = plan.getActionList();
            for(String uuid : actionMap.keySet()){
                Action<Object> action = actionMap.get(uuid);
                action.agentName = plan.getAgent();
                try{
                    AgentService.AgentExecuteResult result = this.getAgentService().executeAction(action);
                    this.getExecutedPlansMap().get(this.getPlan()).put(
                            actionMap.get(uuid), result
                    );
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public PlanExecutor(@Autowired AgentService agentService){
        this.setAgentService(agentService);
    }

    public State execute(Plan plan){
        synchronized (this) {
            boolean running = threadExecutor.getActiveCount() != 0;
            if(!running){
                threadExecutor.execute(new Executor(plan,
                        this.getAgentService(),
                        this.getExecutedPlansMap()));
                this.getExecutedPlansMap().put(plan, new ConcurrentHashMap<>());
                return State.RUNNING;
            }else return State.OTHER_PLAN_IS_RUNNING;
        }
    }
}
