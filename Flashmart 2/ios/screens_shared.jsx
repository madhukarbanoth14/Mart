/* ============================================================
   Flashmart iOS — Shared modules
   Wallet/Ledger · Notifications · Document center
   Dealer: Add SKU · Product approvals
   Employee: CRM list · Follow-up detail · Onboard form
   ============================================================ */
const FS = window.FM;

/* ──────────────── WALLET / LEDGER ──────────────── */
function SK_Wallet() {
  const txns = [
    { type:"debit", label:"ORD-1126 · 3 items", date:"Jun 6", amt:3998 },
    { type:"credit", label:"UPI payment received", date:"Jun 5", amt:5000 },
    { type:"debit", label:"ORD-1122 · 5 items", date:"Jun 4", amt:6240 },
    { type:"debit", label:"ORD-1119 · 2 items", date:"Jun 2", amt:2180 },
    { type:"credit", label:"UPI payment received", date:"Jun 1", amt:8000 },
  ];
  return (
    <Screen>
      <TopBar title="Wallet & ledger" onBack right={<GlyphBtn name="download"/>}/>
      <div style={{padding:"0 16px",display:"flex",flexDirection:"column",gap:14}}>
        {/* balance hero */}
        <div style={{borderRadius:22,padding:22,color:"#fff",position:"relative",overflow:"hidden",
          background:"linear-gradient(150deg,var(--brand),var(--brand-700))",boxShadow:"var(--sh-lg)"}}>
          <div style={{position:"absolute",right:-30,top:-30,width:160,height:160,borderRadius:"50%",background:"rgba(255,255,255,.08)"}}/>
          <div style={{fontSize:13,fontWeight:600,opacity:.85}}>Outstanding balance</div>
          <div className="mono" style={{fontSize:38,fontWeight:700,letterSpacing:"-.03em",margin:"8px 0 4px"}}>{FS.inr(8240)}</div>
          <div style={{fontSize:12.5,opacity:.82}}>Credit limit {FS.inr(25000)} · Available {FS.inr(16760)}</div>
          <div style={{marginTop:18,height:6,borderRadius:99,background:"rgba(255,255,255,.25)",overflow:"hidden"}}>
            <div style={{width:"33%",height:"100%",background:"#fff",borderRadius:99}}/>
          </div>
          <div style={{fontSize:11.5,opacity:.8,marginTop:7}}>33% of credit limit used</div>
          <div style={{marginTop:16}}>
            <Button variant="outline" size="md" icon="wallet" style={{background:"rgba(255,255,255,.15)",color:"#fff",border:"1px solid rgba(255,255,255,.35)"}}>Pay outstanding</Button>
          </div>
        </div>
        {/* quick stats */}
        <div style={{display:"grid",gridTemplateColumns:"1fr 1fr",gap:11}}>
          {[["This month",FS.inr(42600),"var(--brand-tint)","var(--brand)"],["Paid to date",FS.inr(186400),"var(--pos-tint)","var(--pos)"]].map(([l,v,bg,fg])=>(
            <Card key={l} pad={15}><div style={{fontSize:11.5,color:"var(--ink-4)",fontWeight:600,marginBottom:8}}>{l}</div><div className="mono" style={{fontSize:20,fontWeight:700,color:"var(--ink)"}}>{v}</div></Card>
          ))}
        </div>
        {/* ledger */}
        <SectionLabel>Transaction ledger</SectionLabel>
        <Card pad={6}>
          {txns.map((tx,i)=>(
            <div key={i} style={{display:"flex",alignItems:"center",gap:13,padding:"12px 8px",borderBottom:i<txns.length-1?"1px solid var(--line)":"none"}}>
              <div style={{width:38,height:38,borderRadius:11,flexShrink:0,
                background:tx.type==="credit"?"var(--pos-tint)":"var(--surface-2)",
                color:tx.type==="credit"?"var(--pos)":"var(--ink-3)",display:"grid",placeItems:"center"}}>
                <Icon name={tx.type==="credit"?"arrowD":"bag"} size={18} stroke={2.2}/>
              </div>
              <div style={{flex:1,minWidth:0}}>
                <div style={{fontSize:13.5,fontWeight:600,overflow:"hidden",textOverflow:"ellipsis",whiteSpace:"nowrap"}}>{tx.label}</div>
                <div style={{fontSize:12,color:"var(--ink-4)",marginTop:1}}>{tx.date}</div>
              </div>
              <span className="mono" style={{fontSize:14,fontWeight:700,color:tx.type==="credit"?"var(--pos)":"var(--ink)",flexShrink:0}}>
                {tx.type==="credit"?"+":"-"}{FS.inr(tx.amt)}
              </span>
            </div>
          ))}
        </Card>
      </div>
    </Screen>
  );
}

/* ──────────────── NOTIFICATIONS ──────────────── */
function IOS_Notifications() {
  const groups=[
    {day:"Today",items:[
      ["truck","Out for delivery","ORD-1122 is on the way to your store.","var(--brand-tint)","var(--brand)",true],
      ["check","Order delivered","ORD-1124 delivered. Tap to rate.","var(--pos-tint)","var(--pos)",false],
    ]},
    {day:"Yesterday",items:[
      ["doc","Document verified","Your GST certificate was approved.","var(--blue-tint)","var(--blue)",false],
      ["tag","Offer just for you","Extra 8% off staples this week.","var(--gold-tint)","var(--gold-ink)",false],
    ]},
  ];
  return (
    <Screen>
      <TopBar title="Notifications" right={<GlyphBtn name="check"/>}/>
      <div style={{padding:"0 16px",display:"flex",flexDirection:"column",gap:18}}>
        {groups.map(g=>(
          <div key={g.day}>
            <div style={{fontSize:12.5,fontWeight:700,color:"var(--ink-4)",padding:"0 4px 8px"}}>{g.day}</div>
            <Card pad={6}>
              {g.items.map(([ic,t,s,bg,fg,unread],i)=>(
                <div key={t} style={{display:"flex",gap:13,alignItems:"flex-start",padding:"13px 8px",borderBottom:i<g.items.length-1?"1px solid var(--line)":"none",position:"relative"}}>
                  <div style={{width:42,height:42,borderRadius:13,background:bg,color:fg,display:"grid",placeItems:"center",flexShrink:0}}><Icon name={ic} size={20}/></div>
                  <div style={{flex:1}}><div style={{fontSize:14.5,fontWeight:700}}>{t}</div><div style={{fontSize:13,color:"var(--ink-3)",marginTop:2,lineHeight:1.4}}>{s}</div></div>
                  {unread&&<span style={{width:9,height:9,borderRadius:99,background:"var(--brand)",flexShrink:0,marginTop:6}}/>}
                </div>
              ))}
            </Card>
          </div>
        ))}
      </div>
    </Screen>
  );
}

/* ──────────────── DOCUMENT CENTER ──────────────── */
function IOS_DocCenter() {
  const docs=[["Aadhaar card","doc","Verified","Jun 2, 2026"],["PAN card","card","Verified","Jun 2, 2026"],["GST certificate","receipt","Verified","Jun 4, 2026"],["Trade license","layers","Pending","Uploaded Jun 22"]];
  return (
    <Screen>
      <TopBar title="Document center" subtitle="3 verified · 1 pending" onBack/>
      <div style={{padding:"0 16px",display:"flex",flexDirection:"column",gap:12}}>
        <Card style={{display:"flex",alignItems:"center",gap:14}}>
          <div style={{position:"relative",width:52,height:52}}>
            <svg width="52" height="52" viewBox="0 0 52 52"><circle cx="26" cy="26" r="22" fill="none" stroke="var(--surface-3)" strokeWidth="6"/><circle cx="26" cy="26" r="22" fill="none" stroke="var(--pos)" strokeWidth="6" strokeLinecap="round" strokeDasharray={`${0.75*138} 138`} transform="rotate(-90 26 26)"/></svg>
            <span className="mono" style={{position:"absolute",inset:0,display:"grid",placeItems:"center",fontSize:13,fontWeight:700}}>75%</span>
          </div>
          <div style={{flex:1}}><div style={{fontSize:15,fontWeight:700}}>Verification status</div><div style={{fontSize:13,color:"var(--ink-3)",marginTop:2,lineHeight:1.4}}>One more document to fully unlock credit.</div></div>
        </Card>
        {docs.map(([name,ic,status,date])=>{
          const ok=status==="Verified";
          return (
            <Card key={name} style={{display:"flex",alignItems:"center",gap:14}}>
              <div style={{width:44,height:44,borderRadius:13,background:ok?"var(--pos-tint)":"var(--gold-tint)",color:ok?"var(--pos)":"var(--gold-ink)",display:"grid",placeItems:"center",flexShrink:0}}><Icon name={ic} size={20}/></div>
              <div style={{flex:1}}><div style={{fontSize:14.5,fontWeight:700}}>{name}</div><div style={{fontSize:12,color:"var(--ink-4)",marginTop:1}}>{date}</div></div>
              <Badge status={ok?"Approved":status} label={status} size="sm"/>
            </Card>
          );
        })}
        <Button variant="primary" full icon="upload" size="lg">Upload document</Button>
      </div>
    </Screen>
  );
}

/* ──────────────── DEALER: ADD SKU ──────────────── */
function DL_AddSKU() {
  const fields=[["Product name","box","Maggi 2-Minute Noodles"],["Brand","tag","Nestlé"],["Category","grid","Snacks"]];
  return (
    <Screen>
      <TopBar title="Add SKU" onBack/>
      <div style={{padding:"4px 16px 110px",display:"flex",flexDirection:"column",gap:14}}>
        {/* image */}
        <div style={{display:"flex",gap:12,alignItems:"center"}}>
          <div style={{width:80,height:80,borderRadius:16,border:"1.5px dashed var(--line-2)",background:"var(--surface-2)",display:"grid",placeItems:"center",color:"var(--ink-4)",flexShrink:0}}>
            <div style={{textAlign:"center"}}><Icon name="upload" size={22} style={{margin:"0 auto 4px"}}/><div style={{fontSize:10.5,fontWeight:600}}>Photo</div></div>
          </div>
          <div style={{width:80,height:80,borderRadius:16,overflow:"hidden",flexShrink:0}}><ProductThumb p={FS.PRODUCTS[5]} size={80} radius={16}/></div>
          <div style={{fontSize:13,color:"var(--ink-4)",lineHeight:1.5}}>Add up to 4 product photos. First image is the thumbnail.</div>
        </div>
        {fields.map(([label,ic,val])=>(
          <label key={label} style={{display:"block"}}>
            <div style={{fontSize:13,fontWeight:600,color:"var(--ink-3)",marginBottom:6}}>{label}</div>
            <div style={{display:"flex",alignItems:"center",gap:10,height:50,padding:"0 14px",background:"var(--surface)",borderRadius:13,border:"1.5px solid var(--line-2)"}}>
              <Icon name={ic} size={18} color="var(--ink-4)"/>
              <span style={{flex:1,fontSize:15,color:"var(--ink)"}}>{val}</span>
            </div>
          </label>
        ))}
        <div style={{display:"flex",gap:12}}>
          {[["Price (₹)","₹168"],["GST %","18%"],["Stock","410"]].map(([l,v])=>(
            <label key={l} style={{flex:1}}>
              <div style={{fontSize:12,fontWeight:600,color:"var(--ink-3)",marginBottom:6}}>{l}</div>
              <div style={{height:50,padding:"0 14px",background:"var(--surface)",borderRadius:13,border:"1.5px solid var(--line-2)",display:"flex",alignItems:"center"}}>
                <span className="mono" style={{fontSize:15,fontWeight:700,color:"var(--ink)"}}>{v}</span>
              </div>
            </label>
          ))}
        </div>
        <div style={{display:"flex",alignItems:"center",justifyContent:"space-between",padding:"4px 2px"}}>
          <div><div style={{fontSize:14.5,fontWeight:700}}>List immediately</div><div style={{fontSize:12.5,color:"var(--ink-3)"}}>Sends for admin approval</div></div>
          <div style={{width:52,height:32,borderRadius:99,background:"var(--brand)",position:"relative",flexShrink:0}}>
            <span style={{position:"absolute",top:4,right:4,width:24,height:24,borderRadius:99,background:"#fff",boxShadow:"0 1px 3px rgba(0,0,0,.2)"}}/>
          </div>
        </div>
      </div>
      <div style={{position:"absolute",left:0,right:0,bottom:0,padding:"14px 16px 30px",background:"linear-gradient(to top,var(--surface) 64%,transparent)"}}>
        <Button variant="primary" size="lg" full icon="check">Save SKU</Button>
      </div>
    </Screen>
  );
}

/* ──────────────── DEALER: PRODUCT APPROVALS ──────────────── */
function DL_Approvals() {
  const rows=[
    {p:FS.PRODUCTS[5],status:"Approved",note:"Listed at ₹168. GST verified."},
    {p:FS.PRODUCTS[10],status:"Pending",note:"Awaiting admin review · since Jun 5"},
    {p:FS.PRODUCTS[12],status:"Rejected",note:"Price exceeds MRP cap. Revise to ₹185."},
  ];
  return (
    <Screen>
      <TopBar title="Product approvals" subtitle="3 submissions" onBack/>
      <div style={{padding:"4px 16px",display:"flex",flexDirection:"column",gap:11}}>
        {rows.map(({p,status,note})=>(
          <Card key={p.id} pad={14}>
            <div style={{display:"flex",alignItems:"center",gap:12,marginBottom:10}}>
              <ProductThumb p={p} size={44}/>
              <div style={{flex:1,minWidth:0}}><div style={{fontSize:14,fontWeight:700}}>{p.name}</div><div className="mono" style={{fontSize:12,color:"var(--ink-4)",marginTop:1}}>{FS.inr(p.price)}</div></div>
              <Badge status={status} size="sm"/>
            </div>
            <div style={{display:"flex",gap:9,alignItems:"flex-start",background:"var(--surface-2)",borderRadius:12,padding:"10px 12px"}}>
              <Icon name={status==="Rejected"?"bell":"doc"} size={15} color={status==="Rejected"?"var(--neg)":"var(--ink-4)"} style={{marginTop:1}}/>
              <span style={{fontSize:12.5,color:"var(--ink-3)",lineHeight:1.45}}>{note}</span>
            </div>
          </Card>
        ))}
      </div>
    </Screen>
  );
}

/* ──────────────── EMPLOYEE: CRM ──────────────── */
function EM_CRM() {
  return (
    <Screen nav={<NavBar items={emNav("network")} active="network"/>}>
      <TopBar title="CRM" subtitle="10 dealers · 85 shopkeepers" right={<GlyphBtn name="search"/>}/>
      <div style={{padding:"0 16px 12px"}}><Segmented full value="Dealers" onChange={()=>{}} options={["Dealers","Shopkeepers"]}/></div>
      <div className="fm-scroll" style={{display:"flex",gap:8,padding:"0 16px 12px",overflowX:"auto"}}>
        {["All areas","Doc pending","Active","Follow-up due"].map((c,i)=>(
          <span key={c} style={{flexShrink:0,padding:"7px 13px",borderRadius:10,border:"1px solid",borderColor:i===0?"transparent":"var(--line-2)",background:i===0?"var(--ink)":"var(--surface)",color:i===0?"#fff":"var(--ink-3)",fontSize:13,fontWeight:600}}>{c}</span>
        ))}
      </div>
      <div style={{padding:"0 16px",display:"flex",flexDirection:"column",gap:10}}>
        {FS.DEALERS.map((d,i)=>{
          const docPending=i===1||i===4;
          return (
            <Card key={d.id} pad={14}>
              <div style={{display:"flex",alignItems:"center",gap:12}}>
                <Avatar name={d.name} size={42} tint="var(--blue)"/>
                <div style={{flex:1,minWidth:0}}><div style={{fontSize:14.5,fontWeight:700,overflow:"hidden",textOverflow:"ellipsis",whiteSpace:"nowrap"}}>{d.name}</div><div className="mono" style={{fontSize:12,color:"var(--ink-3)",marginTop:1}}>{d.phone} · {d.area}</div></div>
                {docPending?<Badge status="Pending" size="sm"/>:<Badge status="Approved" label="Verified" size="sm"/>}
              </div>
              <div style={{display:"flex",alignItems:"center",justifyContent:"space-between",marginTop:11,paddingTop:11,borderTop:"1px solid var(--line)"}}>
                <span style={{fontSize:12,color:"var(--ink-4)"}}>Follow-up · <b style={{color:"var(--ink)"}}>Jun 26</b></span>
                <div style={{display:"flex",gap:8}}>
                  <div style={{width:36,height:36,borderRadius:11,background:"var(--brand-tint)",color:"var(--pos)",display:"grid",placeItems:"center"}}>
                    <svg width="19" height="19" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2a10 10 0 0 0-8.6 15l-1.3 4.7 4.8-1.3A10 10 0 1 0 12 2Zm5.3 14.1c-.2.6-1.2 1.2-1.7 1.2-.4 0-1 .1-3.3-.9-2.8-1.2-4.5-4-4.6-4.2-.1-.2-1.1-1.4-1.1-2.7s.7-1.9.9-2.2c.2-.2.5-.3.6-.3h.5c.2 0 .4 0 .6.5l.8 2c.1.2.1.4 0 .5l-.4.5c-.2.2-.3.3-.1.6.2.3.8 1.3 1.7 2.1 1.2 1 2.1 1.4 2.4 1.5.2.1.4.1.6-.1l.7-.9c.2-.2.4-.2.6-.1l1.9.9c.3.1.4.2.5.3.1.3.1.7-.1 1.2Z"/></svg>
                  </div>
                  <div style={{width:36,height:36,borderRadius:11,background:"var(--surface-2)",color:"var(--ink-2)",display:"grid",placeItems:"center"}}><Icon name="phone" size={18}/></div>
                </div>
              </div>
            </Card>
          );
        })}
      </div>
    </Screen>
  );
}

/* ──────────────── EMPLOYEE: FOLLOW-UP DETAIL ──────────────── */
function EM_FollowUp() {
  const steps=[
    {label:"Onboarding started",time:"Jun 1 · Neha K.",state:"done"},
    {label:"Aadhaar uploaded & verified",time:"Jun 2",state:"done"},
    {label:"GST document requested",time:"Jun 4 · via WhatsApp",state:"done"},
    {label:"Awaiting GST upload",time:"Follow-up due Jun 26",state:"active"},
    {label:"Account activated",time:"Pending",state:"todo"},
  ];
  return (
    <Screen>
      <TopBar title="Gupta Trading Co." subtitle="DLR-02 · Ghatkopar" onBack right={<GlyphBtn name="phone"/>}/>
      <div style={{padding:"4px 16px",display:"flex",flexDirection:"column",gap:14}}>
        <div style={{display:"flex",gap:10}}>
          <Button variant="ghost" full icon="phone">Call</Button>
          <Button variant="primary" full>Add remark</Button>
        </div>
        <Card>
          <SectionLabel style={{paddingBottom:14}}>Verification timeline</SectionLabel>
          <OrderTimeline steps={steps}/>
        </Card>
        <Card style={{display:"flex",alignItems:"flex-start",gap:12}}>
          <div style={{width:40,height:40,borderRadius:12,background:"var(--gold-tint)",color:"var(--gold-ink)",display:"grid",placeItems:"center",flexShrink:0}}><Icon name="doc" size={20}/></div>
          <div style={{flex:1}}>
            <div style={{fontSize:14,fontWeight:700}}>Last remark</div>
            <div style={{fontSize:13,color:"var(--ink-3)",marginTop:3,lineHeight:1.45}}>"Owner travelling till Jun 25, will share GST cert by 26th. Confirmed on WhatsApp."</div>
            <div style={{fontSize:11.5,color:"var(--ink-4)",marginTop:6}}>Neha K. · Jun 22</div>
          </div>
        </Card>
      </div>
    </Screen>
  );
}

Object.assign(window,{SK_Wallet,IOS_Notifications,IOS_DocCenter,DL_AddSKU,DL_Approvals,EM_CRM,EM_FollowUp});
