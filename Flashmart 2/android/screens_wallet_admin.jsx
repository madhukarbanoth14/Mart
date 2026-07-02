/* ============================================================
   Flashmart Android — Wallet/Ledger + Admin Dashboard (M3)
   ============================================================ */
const WF = window.FM;

/* ──────────────── SHOPKEEPER WALLET ──────────────── */
function AND_SK_Wallet() {
  const txns=[
    {type:"debit",label:"ORD-1126 · 3 items",date:"Jun 6",amt:3998},
    {type:"credit",label:"UPI payment received",date:"Jun 5",amt:5000},
    {type:"debit",label:"ORD-1122 · 5 items",date:"Jun 4",amt:6240},
    {type:"debit",label:"ORD-1119 · 2 items",date:"Jun 2",amt:2180},
    {type:"credit",label:"UPI payment received",date:"Jun 1",amt:8000},
  ];
  return (
    <M3Screen topBar={<M3TopBar onBack title="Wallet & ledger" actions={<M3IconBtn icon="download"/>}/>}>
      <div style={{padding:"4px 16px",display:"flex",flexDirection:"column",gap:14}}>
        {/* balance hero */}
        <div style={{borderRadius:24,padding:22,color:"#fff",position:"relative",overflow:"hidden",
          background:"linear-gradient(150deg,var(--brand),var(--brand-700))",boxShadow:"var(--m3-e2)"}}>
          <div style={{position:"absolute",right:-30,top:-30,width:160,height:160,borderRadius:"50%",background:"rgba(255,255,255,.08)"}}/>
          <div style={{position:"relative"}}>
            <div style={{fontSize:13,fontWeight:600,opacity:.85}}>Outstanding balance</div>
            <div className="mono" style={{fontSize:36,fontWeight:700,letterSpacing:"-.03em",margin:"8px 0 4px"}}>{WF.inr(8240)}</div>
            <div style={{fontSize:12.5,opacity:.82}}>Limit {WF.inr(25000)} · Available {WF.inr(16760)}</div>
            <div style={{height:6,borderRadius:99,background:"rgba(255,255,255,.25)",overflow:"hidden",marginTop:14}}>
              <div style={{width:"33%",height:"100%",background:"#fff",borderRadius:99}}/>
            </div>
            <div style={{fontSize:11.5,opacity:.8,marginTop:6}}>33% of credit limit used</div>
            <div style={{marginTop:16}}>
              <M3Button variant="gold" size="sm" icon="wallet">Pay outstanding</M3Button>
            </div>
          </div>
        </div>
        {/* quick stats */}
        <div style={{display:"grid",gridTemplateColumns:"1fr 1fr",gap:11}}>
          {[["This month",WF.inr(42600),"var(--brand-tint)","var(--brand)"],
            ["Paid to date",WF.inr(186400),"var(--pos-tint)","var(--pos)"]].map(([l,v,bg,fg])=>(
            <M3Card key={l} variant="filled" pad={15}>
              <div style={{fontSize:11.5,color:M.onSurfVar,fontWeight:600,marginBottom:8}}>{l}</div>
              <div className="mono" style={{fontSize:20,fontWeight:700,color:M.onSurf}}>{v}</div>
            </M3Card>
          ))}
        </div>
        {/* ledger */}
        <M3SectionLabel>Transaction ledger</M3SectionLabel>
        <M3Card variant="filled" pad={6}>
          {txns.map((tx,i)=>(
            <M3ListItem key={i} last={i===txns.length-1}
              leading={<div style={{width:40,height:40,borderRadius:12,flexShrink:0,
                background:tx.type==="credit"?"var(--pos-tint)":M.surf4,
                color:tx.type==="credit"?"var(--pos)":M.onSurfVar,display:"grid",placeItems:"center"}}>
                <Icon name={tx.type==="credit"?"arrowD":"bag"} size={18} stroke={2.2}/>
              </div>}
              headline={tx.label} supporting={tx.date}
              trailing={<span className="mono" style={{fontSize:14,fontWeight:700,color:tx.type==="credit"?"var(--pos)":M.onSurf}}>
                {tx.type==="credit"?"+":"-"}{WF.inr(tx.amt)}
              </span>}/>
          ))}
        </M3Card>
      </div>
    </M3Screen>
  );
}

/* ──────────────── mini bar chart ──────────────── */
function AND_MiniBar({data,color="var(--brand)",h=52}) {
  const max=Math.max(...data);
  return (
    <div style={{display:"flex",gap:4,alignItems:"flex-end",height:h}}>
      {data.map((v,i)=>(
        <div key={i} style={{flex:1,borderRadius:"4px 4px 0 0",
          background:i===data.length-1?color:`${color}55`,height:`${(v/max)*100}%`}}/>
      ))}
    </div>
  );
}

/* ──────────────── ADMIN DASHBOARD ──────────────── */
const adNavItemsAnd = ()=>[
  {id:"home",icon:"home",label:"Home"},
  {id:"dealers",icon:"users",label:"Dealers"},
  {id:"products",icon:"box",label:"Products",badge:3},
  {id:"profile",icon:"user",label:"Admin"},
];

function AND_ADMIN_Home() {
  const gmvData=[31,28,45,38,52,49,61,55,68,72,58,78];
  return (
    <M3Screen nav={<M3NavBar items={adNavItemsAnd()} active="home"/>}>
      <div style={{padding:`${AND_TOP+6}px 12px 12px 20px`,display:"flex",alignItems:"center",gap:12}}>
        <div style={{flex:1,minWidth:0}}>
          <div style={{fontSize:13,fontWeight:700,color:"var(--brand)"}}>Operations dashboard</div>
          <div style={{fontSize:22,fontWeight:800,letterSpacing:"-.025em",color:M.onSurf}}>Admin</div>
        </div>
        <M3IconBtn icon="bell" filled badge={3}/>
        <Avatar name="Admin" size={42} tint="var(--brand)"/>
      </div>
      <div style={{padding:"0 16px",display:"flex",flexDirection:"column",gap:14}}>
        {/* GMV hero */}
        <M3Card variant="elevated" pad={20} style={{background:"var(--ink-surface)",overflow:"hidden",position:"relative"}}>
          <div style={{position:"absolute",right:-20,top:-20,width:130,height:130,borderRadius:"50%",background:"rgba(255,255,255,.05)"}}/>
          <div style={{fontSize:12.5,fontWeight:600,color:"rgba(255,255,255,.7)"}}>GMV · Jun 2026</div>
          <div className="mono" style={{fontSize:32,fontWeight:700,letterSpacing:"-.03em",color:"#fff",margin:"8px 0 4px"}}>{WF.inrShort(4850000)}</div>
          <div style={{fontSize:12.5,color:"rgba(255,255,255,.6)",marginBottom:16}}>↑ 18% vs last month · 342 orders</div>
          <AND_MiniBar data={gmvData} color="var(--brand)" h={52}/>
          <div style={{display:"flex",justifyContent:"space-between",marginTop:4,fontSize:10,color:"rgba(255,255,255,.45)"}}>
            <span>Jun 1</span><span>Today</span>
          </div>
        </M3Card>
        {/* stats */}
        <div style={{display:"grid",gridTemplateColumns:"1fr 1fr",gap:11}}>
          {[["users","Dealers","10","var(--blue-tint)","var(--blue)"],
            ["bag","Shopkeepers","85","var(--brand-tint)","var(--brand)"],
            ["clock","Pending KYC","5","var(--gold-tint)","var(--gold-ink)"],
            ["box","SKU approvals","3","var(--neg-tint)","var(--m3-error)"]].map(([ic,l,v,bg,fg])=>(
            <M3Card key={l} variant="filled" pad={15}>
              <div style={{width:36,height:36,borderRadius:10,background:bg,color:fg,display:"grid",placeItems:"center",marginBottom:10}}><Icon name={ic} size={19}/></div>
              <div className="mono" style={{fontSize:22,fontWeight:700,color:M.onSurf}}>{v}</div>
              <div style={{fontSize:12.5,color:M.onSurfVar,fontWeight:600,marginTop:1}}>{l}</div>
            </M3Card>
          ))}
        </div>
        {/* area performance */}
        <M3SectionLabel>Area performance</M3SectionLabel>
        <M3Card variant="filled" pad={6}>
          {[["Andheri East",WF.inrShort(412000),86,100],["Dadar",WF.inrShort(298500),64,76],["Ghatkopar",WF.inrShort(241200),52,62],["Thane West",WF.inrShort(176800),38,45]].map(([area,rev,orders,pct],i)=>(
            <div key={area} style={{padding:"12px 8px",borderBottom:i<3?`1px solid ${M.outlineVar}`:"none"}}>
              <div style={{display:"flex",justifyContent:"space-between",marginBottom:7}}>
                <span style={{fontSize:14,fontWeight:700,color:M.onSurf}}>{area}</span>
                <span className="mono" style={{fontSize:13.5,fontWeight:700,color:M.onSurf}}>{rev}</span>
              </div>
              <div style={{display:"flex",alignItems:"center",gap:10}}>
                <div style={{flex:1,height:5,borderRadius:99,background:M.surf4,overflow:"hidden"}}><div style={{width:pct+"%",height:"100%",borderRadius:99,background:"var(--brand)"}}/></div>
                <span style={{fontSize:11.5,color:M.onSurfVar,fontWeight:600,minWidth:48,textAlign:"right"}}>{orders} orders</span>
              </div>
            </div>
          ))}
        </M3Card>
      </div>
    </M3Screen>
  );
}

/* ──────────────── ADMIN: DEALER LIST ──────────────── */
function AND_ADMIN_Dealers() {
  return (
    <M3Screen nav={<M3NavBar items={adNavItemsAnd()} active="dealers"/>}
      topBar={<M3TopBar variant="large" title="Dealers" subtitle="10 active · 2 onboarding" actions={<M3IconBtn icon="plus"/>}/>}
      fab={<M3FAB icon="plus" label="Add dealer" color="primary"/>}>
      <div style={{padding:"0 16px 12px"}}><M3Segmented full value="Active" onChange={()=>{}} options={["Active","Onboarding","Suspended"]}/></div>
      <div style={{padding:"0 16px",display:"flex",flexDirection:"column",gap:10}}>
        {WF.DEALERS.map((d,i)=>{
          const rev=[WF.inrShort(412000),WF.inrShort(298500),WF.inrShort(241200),WF.inrShort(176800),WF.inrShort(94300)][i]||"—";
          return (
            <M3Card key={d.id} variant="outlined" pad={14}>
              <div style={{display:"flex",alignItems:"center",gap:13,marginBottom:11}}>
                <Avatar name={d.name} size={44} tint="var(--blue)"/>
                <div style={{flex:1,minWidth:0}}>
                  <div style={{fontSize:14.5,fontWeight:700,overflow:"hidden",textOverflow:"ellipsis",whiteSpace:"nowrap",color:M.onSurf}}>{d.name}</div>
                  <div className="mono" style={{fontSize:12,color:M.onSurfVar,marginTop:1}}>{d.id} · {d.area}</div>
                </div>
                <M3Status status={i===4?"Pending":"Active"} label={i===4?"Onboarding":"Active"} size="sm" dot/>
              </div>
              <div style={{display:"flex",gap:10}}>
                {[["Orders",d.orders],["Revenue",rev]].map(([l,v])=>(
                  <div key={l} style={{flex:1,background:M.surf3,borderRadius:10,padding:"9px 12px",textAlign:"center"}}>
                    <div className="mono" style={{fontSize:14,fontWeight:700,color:M.onSurf}}>{v}</div>
                    <div style={{fontSize:11,color:M.onSurfVar,marginTop:2}}>{l}</div>
                  </div>
                ))}
                <div style={{flex:1,background:M.primCont,borderRadius:10,padding:"9px 12px",textAlign:"center",color:M.onPrimCont}}>
                  <div style={{fontSize:13,fontWeight:700}}>View</div>
                  <div style={{fontSize:11,marginTop:2}}>Details</div>
                </div>
              </div>
            </M3Card>
          );
        })}
      </div>
    </M3Screen>
  );
}

/* ──────────────── ADMIN: SKU APPROVALS ──────────────── */
function AND_ADMIN_Approvals() {
  const pending=[
    {p:WF.PRODUCTS[10],dealer:"Shree Balaji",note:"New SKU · ₹320 · 18% GST · 95 in stock"},
    {p:WF.PRODUCTS[12],dealer:"Krishna Wholesale",note:"Price revised from ₹215 → ₹200"},
    {p:WF.PRODUCTS[13],dealer:"Gupta Trading Co.",note:"New SKU · ₹99 · 18% GST · 320 in stock"},
  ];
  return (
    <M3Screen nav={<M3NavBar items={adNavItemsAnd()} active="products"/>}
      topBar={<M3TopBar variant="large" title="SKU approvals" subtitle="3 pending · 1 rejected"/>}>
      <div style={{padding:"0 16px 12px"}}><M3Segmented full value="Pending" onChange={()=>{}} options={["Pending","Approved","Rejected"]}/></div>
      <div style={{padding:"0 16px",display:"flex",flexDirection:"column",gap:11}}>
        {pending.map(({p,dealer,note})=>(
          <M3Card key={p.id} variant="outlined" pad={14}>
            <div style={{display:"flex",alignItems:"center",gap:12,marginBottom:12}}>
              <ProductThumb p={p} size={52}/>
              <div style={{flex:1,minWidth:0}}>
                <div style={{fontSize:14.5,fontWeight:700,lineHeight:1.2,color:M.onSurf}}>{p.name}</div>
                <div style={{fontSize:13,color:M.onSurfVar,marginTop:2}}>{dealer}</div>
                <div style={{fontSize:12,color:M.onSurfVar,marginTop:2}}>{note}</div>
              </div>
            </div>
            <div style={{display:"flex",gap:10}}>
              <M3Button variant="outlined" size="sm" full style={{color:M.err,borderColor:M.err}}>Reject</M3Button>
              <M3Button variant="filled" size="sm" full icon="check">Approve</M3Button>
            </div>
          </M3Card>
        ))}
      </div>
    </M3Screen>
  );
}

Object.assign(window,{AND_SK_Wallet,AND_MiniBar,adNavItemsAnd,AND_ADMIN_Home,AND_ADMIN_Dealers,AND_ADMIN_Approvals});
