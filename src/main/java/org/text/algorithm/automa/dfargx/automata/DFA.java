package org.text.algorithm.automa.dfargx.automata;

import org.text.algorithm.automa.Dfa;
import org.text.algorithm.automa.dfargx.util.CommonSets;

import java.util.*;

/**
 * Created on 2015/5/10.
 */
public class DFA {

    private final NFABitmapStateManager nfaiStateManager;
    private final List<NFABitmapState> nfaiStates;
    private int is; // init state
    private int rs; // rejected state
    private boolean[] fs; // final states

    private int rootId;
    private char MIN_CHAR;
    private char MAX_CHAR;

    private Dfa dfa;

    public DFA(NFABitmapStateManager nfaiStateManager, int rootId) {
        this.nfaiStateManager = nfaiStateManager;
        this.nfaiStates = nfaiStateManager.getStates();
        is = rs = -1;
        fs = null;
        this.rootId = rootId;
        init();
    }

    public Dfa getDfa() {
        return dfa;
    }

    private void init() {
        MIN_CHAR = Character.MAX_VALUE;
        MAX_CHAR =Character.MIN_VALUE;
        for(NFABitmapState s : nfaiStates) {
            for( char ch : s.getTransitionMap().keySet()) {
                if(ch > MAX_CHAR) {
                    MAX_CHAR = ch;
                }
                if(ch < MIN_CHAR) {
                    MIN_CHAR = ch;
                }
            }
        }
        NFABitmapState initState = nfaiStates.get(0);
        NFABitmapState finalState = nfaiStates.get(1);

        Map<NFABitmapState, NFABitmapStatePack> closureMap = calculateClosure(nfaiStates);

        // construct a NFA first
        Map<NFABitmapState, Map<Character, NFABitmapStatePack>> nfaTransitionMap = new HashMap<>();
        for (NFABitmapState state : nfaiStates) {
            Map<Character, NFABitmapStatePack> subMap = new HashMap<>();
            for (char ch = MIN_CHAR; ch <= MAX_CHAR; ch++) {
                NFABitmapStatePack closure = closureMap.get(state);
                NFABitmapStatePack reachable = traceReachable(closure, ch, closureMap);
                if (!reachable.isEmpty()) {
                    subMap.put(ch, reachable);
                }
            }
            nfaTransitionMap.put(state, subMap);
        }

        // Construct an original DFA using the constructed NFA. Each key which is set of nfa states is a new dfa state.
        Map<NFABitmapStatePack, Map<Character, NFABitmapStatePack>> originalDFATransitionMap = new HashMap<>();
        constructOriginalDFA(closureMap.get(initState), nfaTransitionMap, originalDFATransitionMap);

        // construct minimum DFA
        minimize(originalDFATransitionMap, closureMap.get(initState), finalState);
    }

    private void constructOriginalDFA(NFABitmapStatePack stateSet, Map<NFABitmapState, Map<Character, NFABitmapStatePack>> nfaTransitionMap, Map<NFABitmapStatePack, Map<Character, NFABitmapStatePack>> originalDFATransitionMap) {

        Stack<NFABitmapStatePack> stack = new Stack<>();
        stack.push(stateSet);

        do {
            NFABitmapStatePack pop = stack.pop();
            Map<Character, NFABitmapStatePack> subMap = originalDFATransitionMap.get(pop);
            if (subMap == null) {
                subMap = new HashMap<>();
                originalDFATransitionMap.put(pop, subMap);
            }
            for (char ch = MIN_CHAR; ch <= MAX_CHAR; ch++) {
                NFABitmapStatePack union = nfaiStateManager.newEmptyPack();
                for (NFABitmapState state : pop.asList()) {
                    NFABitmapStatePack nfaSet = nfaTransitionMap.get(state).get(ch);
                    if (nfaSet != null) {
                        union.addAll(nfaSet);
                    }
                }
                union.freeze();
                if (!union.isEmpty()) {
                    subMap.put(ch, union);
                    if (!originalDFATransitionMap.containsKey(union)) {
                        stack.push(union);
                    }
                }
            }
        } while (!stack.isEmpty());
    }

    private Map<NFABitmapState, NFABitmapStatePack> calculateClosure(List<NFABitmapState> nfaStateList) {
        Map<NFABitmapState, NFABitmapStatePack> map = new HashMap<>();
        for (NFABitmapState state : nfaStateList) {
            NFABitmapStatePack closure = nfaiStateManager.newEmptyPack();
            dfsClosure(state, closure);
            closure.freeze();
            map.put(state, closure);
        }
        return map;
    }

    private void dfsClosure(NFABitmapState state, NFABitmapStatePack closure) {
        Stack<NFABitmapState> nfaStack = new Stack<>();
        nfaStack.push(state);
        do {
            NFABitmapState pop = nfaStack.pop();
            closure.addState(pop.getId());
            for (NFABitmapState next : pop.getDirectTable().asList()) {
                if (!closure.contains(next.getId())) {
                    nfaStack.push(next);
                }
            }
        } while (!nfaStack.isEmpty());
    }

    private NFABitmapStatePack traceReachable(NFABitmapStatePack closure, char ch, Map<NFABitmapState, NFABitmapStatePack> closureMap) {
        NFABitmapStatePack result = nfaiStateManager.newEmptyPack();
        for (NFABitmapState closureState : closure.asList()) {
            Map<Character, NFABitmapStatePack> transitionMap = closureState.getTransitionMap();
            NFABitmapStatePack stateSet = transitionMap.get(ch);
            if (stateSet != null) {
                for (NFABitmapState state : stateSet.asList()) {
                    result.addAll(closureMap.get(state)); // closure of all the reachable states by scanning a char of the given closure.
                }
            }
        }
        result.freeze();
        return result;
    }

    private void minimize(Map<NFABitmapStatePack, Map<Character, NFABitmapStatePack>> oriDFATransitionMap, NFABitmapStatePack initClosure, NFABitmapState finalNFAIState) {
        Map<Integer, Map<Character,Integer>> renamedDFATransitionTable = new HashMap<>();
        Map<Integer, Boolean> finalFlags = new HashMap<>();
        Map<NFABitmapStatePack, Integer> stateRenamingMap = new HashMap<>();
        int initStateAfterRenaming = -1;
        int renamingStateID = 1;

        // rename all states
        for (NFABitmapStatePack nfaState : oriDFATransitionMap.keySet()) {
            if (initStateAfterRenaming == -1 && nfaState.equals(initClosure)) {
                initStateAfterRenaming = renamingStateID; // preserve init state id
            }
            stateRenamingMap.put(nfaState, renamingStateID++);
        }

        renamedDFATransitionTable.put(0, newRejectedState()); // the rejected state 0
        finalFlags.put(0, false);

        // construct renamed dfa transition table
        for (Map.Entry<NFABitmapStatePack, Map<Character, NFABitmapStatePack>> entry : oriDFATransitionMap.entrySet()) {
            renamingStateID = stateRenamingMap.get(entry.getKey());
            Map<Character,Integer> state = newRejectedState();
            for (Map.Entry<Character, NFABitmapStatePack> row : entry.getValue().entrySet()) {
                state.put(row.getKey(), stateRenamingMap.get(row.getValue()));
            }
            renamedDFATransitionTable.put(renamingStateID, state);
            if (entry.getKey().contains(finalNFAIState.getId())) {
                finalFlags.put(renamingStateID, true);
            } else {
                finalFlags.put(renamingStateID, false);
            }
        }

        // group states to final states and non-final states
        Map<Integer, Integer> groupFlags = new HashMap<>();
        for (int i = 0; i < finalFlags.size(); i++) {
            boolean b = finalFlags.get(i);
            if (b) {
                groupFlags.put(i, 0);
            } else {
                groupFlags.put(i, 1);
            }
        }

        int groupTotal = 2;
        int prevGroupTotal;
        do { // splitting, group id is the final state id
            prevGroupTotal = groupTotal;
            for (int sensitiveGroup = 0; sensitiveGroup < prevGroupTotal; sensitiveGroup++) {
                //  <target group table, state id set>
                Map<Map<Integer, Integer>, Set<Integer>> invertMap = new HashMap<>();
                for (int sid = 0; sid < groupFlags.size(); sid++) { //use state id to iterate
                    int group = groupFlags.get(sid);
                    if (sensitiveGroup == group) {
                        Map<Integer, Integer> targetGroupTable = new HashMap<>(MAX_CHAR - MIN_CHAR);
                        for (char ch = MIN_CHAR; ch <= MAX_CHAR; ch++) {
                            Integer targetState = renamedDFATransitionTable.get(sid).get(ch);
                            if(targetState != null) {
                                int targetGroup = groupFlags.get(targetState);
                                targetGroupTable.put((int) ch, targetGroup);
                            }
                        }
                        Set<Integer> stateIDSet = invertMap.get(targetGroupTable);
                        if (stateIDSet == null) {
                            stateIDSet = new HashSet<>();
                            invertMap.put(targetGroupTable, stateIDSet);
                        }
                        stateIDSet.add(sid);
                    }
                }

                boolean first = true;
                for (Set<Integer> stateIDSet : invertMap.values()) {
                    if (first) {
                        first = false;
                    } else {
                        for (int sid : stateIDSet) {
                            groupFlags.put(sid, groupTotal);
                        }
                        groupTotal++;
                    }
                }
            }
        } while (prevGroupTotal != groupTotal);

        // determine initial group state
        is = groupFlags.get(initStateAfterRenaming);

        // determine rejected group state
        rs = groupFlags.get(0);

        // determine final group states
        Set<Integer> finalGroupFlags = new HashSet<>();
        for (int i = 0, groupFlagsSize = groupFlags.size(); i < groupFlagsSize; i++) {
            Integer groupFlag = groupFlags.get(i);
            if (finalFlags.get(i)) {
                finalGroupFlags.add(groupFlag);
            }
        }
        fs = new boolean[groupTotal];
        for (int i = 0; i < groupTotal; i++) {
            fs[i] = finalGroupFlags.contains(i);
        }

        dfa = new Dfa();
        dfa.setRoot(rootId);

        int[] groupMap = new int[groupTotal];
        groupMap[is] = rootId;
        groupMap[rs] = -1; //INVALID

        int order = rootId + 1;
        for(int i = 0; i < groupTotal; i++) {
            if(i != is && i != rs) {
                groupMap[i] = order++;
            }
        }

        Map<Integer, Dfa.DfaState> states = new HashMap<>();
        dfa.setStates(states);

        for (int groupID = 0; groupID < groupTotal; groupID++) {
            int stateId = groupMap[groupID];
            if(stateId == -1) {
                continue;
            }
            Dfa.DfaState state = new Dfa.DfaState();
            state.setId(stateId);
            if(fs[groupID]) {
                state.setAccept(true);
            }
            states.put(stateId, state);

            for (int sid = 0; sid < groupFlags.size(); sid++) {
                if (groupID == groupFlags.get(sid)) {

                    Map<Character, Integer> oriState = renamedDFATransitionTable.get(sid);
                    for (char ch = MIN_CHAR; ch <= MAX_CHAR; ch++) {
                        Integer next = oriState.get(ch);
                        if(next != null) {
                            int nextState = groupMap[groupFlags.get(next)];
                            if (nextState != -1) {
                                state.addTransition((int)ch, nextState);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }


    private Map<Character, Integer> newRejectedState() {
        return new TreeMap<>();
        //int[] state = new int[CommonSets.ENCODING_LENGTH];
        //rejectAll(state);
        //return state;
    }

    private void rejectAll(int[] state) {
        for (int i = 0; i < state.length; i++) {
            state[i] = 0;
        }
    }
}
