/* ============================================================
   Flashmart iOS — Admin dashboard
   GMV overview · Dealer management · SKU approvals · Analytics
   ============================================================ */
const FA2 = window.FM;
const adNav = () => [
  {id:"home",icon:"home",label:"Home"},
  {id:"dealers",icon:"users",label:"Dealers"},
  {id:"products",icon:"box",label:"Products"},
  {id:"profile",icon:"user",label:"Admin"},
];

/* mini bar chart */
function MiniBar({data,color="var(--brand)",h=48}) {
  const max=Math.max(...data);
  return (
    <div style={{display:"flex",gap:4,alignItems:"flex-end",height:h}}>
      {data.map((v,i)=>(
        <div key={i} style={{flex:1,borderRadius:"4px 4px 0 0",background:i===data.length-1?color:`${color}55`,height:`${(v/max)*100}%`,transition:"height .3s"}}/>
      ))}
    </div>
  );
}

/* ──────────────── ADMIN DASHBOARD ──────────────── */
function ADMIN_Home() {
  const gmvData=[31,28,45,38,52,49,61,55,68,72,58,78];
  return (
    <Screen nav={<NavBar items={adNav()} active="home"/>}>
      <TopBar title="Admin" kicker="FlashMart operations" accent="var(--brand)"
        right={<><GlyphBtn name="bell" badge={3}/><Avatar name="Admin" tint="var(--brand)"/></>}/>
      <div style={{padding:"0 16px",display:"flex",flexDirection:"column",gap:14}}>
        {/* GMV card */}
        <Card pad={18} style={{background:"linear-gradient(150deg,var(--ink-surface),var(--ink-surface-2))",border:"none",color:"#fff"}}>
          <div style={{fontSize:13,fontWeight:600,opacity:.75}}>Gross Merchandise Value · Jun 2026</div>
          <div className="mono" style={{fontSize:36,fontWeight:700,letterSpacing:"-.03em",margin:"8px 0 4px"}}>{FA2.inrShort(4850000)}</div>
          <div style={{fontSize:13,opacity:.75,marginBottom:16}}>↑ 18% vs last month · 342 orders</div>
          <MiniBar data={gmvData} color="var(--brand)" h={52}/>
          <div style={{display:"flex",justifyContent:"space-between",marginTop:4,fontSize:10.5,opacity:.55}}>
            <span>Jun 1</span><span>Today</span>
          </div>
        </Card>
        {/* stats grid */}
        <div style={{display:"grid",gridTemplateColumns:"1fr 1fr",gap:11}}>
          {[["users","Active dealers","10","var(--blue-tint)","var(--blue)"],
            ["bag","Shopkeepers","85","var(--brand-tint)","var(--brand)"],
            ["clock","Pending KYC","5","var(--gold-tint)","var(--gold-ink)"],
            ["box","SKU approvals","3","var(--neg-tint)","var(--neg)"]].map(([ic,l,v,bg,fg])=>(
            <Card key={l} pad={15}>
              <div style={{width:36,height:36,borderRadius:10,background:bg,color:fg,display:"grid",placeItems:"center",marginBottom:10}}><Icon name={ic} size={19}/></div>
              <div className="mono" style={{fontSize:22,fontWeight:700}}>{v}</div>
              <div style={{fontSize:12.5,color:"var(--ink-4)",fontWeight:600,marginTop:1}}>{l}</div>
            </Card>
          ))}
        </div>
        {/* area performance */}
        <SectionLabel>Area performance</SectionLabel>
        <Card pad={6}>
          {[["Andheri East",FA2.inrShort(412000),86,100],["Dadar",FA2.inrShort(298500),64,76],["Ghatkopar",FA2.inrShort(241200),52,62],["Thane West",FA2.inrShort(176800),38,45]].map(([area,rev,orders,pct],i)=>(
            <div key={area} style={{padding:"12px 8px",borderBottom:i<3?"1px solid var(--line)":"none"}}>
              <div style={{display:"flex",justifyContent:"space-between",marginBottom:7}}>
                <span style={{fontSize:14,fontWeight:600}}>{area}</span>
                <span className="mono" style={{fontSize:13.5,fontWeight:700}}>{rev}</span>
              </div>
              <div style={{display:"flex",alignItems:"center",gap:10}}>
                <div style={{flex:1,height:5,borderRadius:99,background:"var(--surface-3)",overflow:"hidden"}}><div style={{width:pct+"%",height:"100%",borderRadius:99,background:"var(--brand)"}}/></div>
                <span style={{fontSize:11.5,color:"var(--ink-4)",fontWeight:600,minWidth:48,textAlign:"right"}}>{orders} orders</span>
              </div>
            </div>
          ))}
        </Card>
      </div>
    </Screen>
  );
}

/* ──────────────── ADMIN: DEALER LIST ──────────────── */
function ADMIN_Dealers() {
  return (
    <Screen nav={<NavBar items={adNav()} active="dealers"/>}>
      <TopBar title="Dealers" subtitle="10 active · 2 onboarding" right={<GlyphBtn name="plus"/>}/>
      <div style={{padding:"0 16px 12px"}}><Segmented full value="Active" onChange={()=>{}} options={["Active","Onboarding","Suspended"]}/></div>
      <div style={{padding:"0 16px",display:"flex",flexDirection:"column",gap:10}}>
        {FA2.DEALERS.map((d,i)=>{
          const rev=[FA2.inrShort(412000),FA2.inrShort(298500),FA2.inrShort(241200),FA2.inrShort(176800),FA2.inrShort(94300)][i]||"—";
          return (
            <Card key={d.id} pad={14}>
              <div style={{display:"flex",alignItems:"center",gap:13,marginBottom:10}}>
                <Avatar name={d.name} size={44} tint="var(--blue)"/>
                <div style={{flex:1,minWidth:0}}>
                  <div style={{fontSize:14.5,fontWeight:700,overflow:"hidden",textOverflow:"ellipsis",whiteSpace:"nowrap"}}>{d.name}</div>
                  <div className="mono" style={{fontSize:12,color:"var(--ink-3)",marginTop:1}}>{d.id} · {d.area}</div>
                </div>
                <Badge status={i===4?"Onboarding":"Active"} size="sm" dot/>
              </div>
              <div style={{display:"flex",gap:10}}>
                <div style={{flex:1,background:"var(--surface-2)",borderRadius:10,padding:"8px 12px",textAlign:"center"}}>
                  <div className="mono" style={{fontSize:14,fontWeight:700}}>{d.orders}</div>
                  <div style={{fontSize:11,color:"var(--ink-4)",marginTop:2}}>Orders</div>
                </div>
                <div style={{flex:1,background:"var(--surface-2)",borderRadius:10,padding:"8px 12px",textAlign:"center"}}>
                  <div className="mono" style={{fontSize:14,fontWeight:700}}>{rev}</div>
                  <div style={{fontSize:11,color:"var(--ink-4)",marginTop:2}}>Revenue</div>
                </div>
                <div style={{flex:1,background:"var(--brand-tint)",borderRadius:10,padding:"8px 12px",textAlign:"center",color:"var(--brand)"}}>
                  <div style={{fontSize:13,fontWeight:700}}>View</div>
                  <div style={{fontSize:11,marginTop:2}}>Details</div>
                </div>
              </div>
            </Card>
          );
        })}
      </div>
    </Screen>
  );
}

/* ──────────────── ADMIN: SKU APPROVALS ──────────────── */
function ADMIN_Approvals() {
  const pending=[
    {p:FA2.PRODUCTS[10],dealer:"Shree Balaji",note:"New SKU · ₹320 · 18% GST · 95 in stock"},
    {p:FA2.PRODUCTS[12],dealer:"Krishna Wholesale",note:"Price revised from ₹215 → ₹200"},
    {p:FA2.PRODUCTS[13],dealer:"Gupta Trading Co.",note:"New SKU · ₹99 · 18% GST · 320 in stock"},
  ];
  return (
    <Screen nav={<NavBar items={adNav()} active="products"/>}>
      <TopBar title="SKU approvals" subtitle="3 pending · 1 rejected"/>
      <div style={{padding:"0 16px 12px"}}><Segmented full value="Pending" onChange={()=>{}} options={["Pending","Approved","Rejected"]}/></div>
      <div style={{padding:"0 16px",display:"flex",flexDirection:"column",gap:11}}>
        {pending.map(({p,dealer,note},i)=>(
          <Card key={p.id} pad={14}>
            <div style={{display:"flex",alignItems:"center",gap:12,marginBottom:11}}>
              <ProductThumb p={p} size={52}/>
              <div style={{flex:1,minWidth:0}}>
                <div style={{fontSize:14.5,fontWeight:700,lineHeight:1.2}}>{p.name}</div>
                <div style={{fontSize:12.5,color:"var(--ink-3)",marginTop:2}}>{dealer}</div>
                <div style={{fontSize:12,color:"var(--ink-4)",marginTop:3}}>{note}</div>
              </div>
            </div>
            <div style={{display:"flex",gap:10}}>
              <Button variant="soft" full icon="check" style={{color:"var(--neg)",background:"var(--neg-tint)"}}>Reject</Button>
              <Button variant="pos" full icon="check">Approve</Button>
            </div>
          </Card>
        ))}
      </div>
    </Screen>
  );
}

Object.assign(window,{adNav,MiniBar,ADMIN_Home,ADMIN_Dealers,ADMIN_Approvals});
