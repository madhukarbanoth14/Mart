/* ============================================================
   Flashmart iOS — Shopkeeper pt.2
   Checkout · Payment · Success · Orders list · Invoice
   ============================================================ */
const F2 = window.FM;

/* ---------------- CHECKOUT ---------------- */
function SK_Checkout() {
  const items = [["atta",6],["oil",12],["salt",10]].map(([id,q])=>({...F2.lookups.product(id),qty:q}));
  const t = F2.orderMath(items);
  return (
    <Screen>
      <TopBar title="Checkout" onBack />
      <div style={{padding:"4px 16px 110px",display:"flex",flexDirection:"column",gap:14}}>
        {/* address */}
        <div>
          <SectionLabel action="Change">Delivery address</SectionLabel>
          <Card style={{display:"flex",gap:13,alignItems:"flex-start"}}>
            <div style={{width:40,height:40,borderRadius:12,background:"var(--brand-tint)",color:"var(--brand)",display:"grid",placeItems:"center",flexShrink:0}}><Icon name="pin" size={20}/></div>
            <div style={{flex:1}}><div style={{fontSize:14.5,fontWeight:700}}>Ramesh General Store</div><div style={{fontSize:13,color:"var(--ink-3)",marginTop:2,lineHeight:1.45}}>Shop 12, Andheri East, Mumbai 400069</div></div>
          </Card>
        </div>
        {/* payment method */}
        <div>
          <SectionLabel>Payment method</SectionLabel>
          <Card pad={6}>
            {[["upi","UPI","GPay · PhonePe · Paytm",true],["card","Card","Visa · Mastercard",false],["wallet","FlashMart credit","₹8,240 outstanding",false]].map(([ic,t2,s,on],i)=>(
              <div key={t2} style={{display:"flex",alignItems:"center",gap:13,padding:"12px 8px",borderBottom:i<2?"1px solid var(--line)":"none"}}>
                <div style={{width:38,height:38,borderRadius:11,background:"var(--surface-2)",display:"grid",placeItems:"center",color:"var(--ink-3)"}}><Icon name={ic} size={19}/></div>
                <div style={{flex:1}}><div style={{fontSize:14.5,fontWeight:700}}>{t2}</div><div style={{fontSize:12,color:"var(--ink-4)"}}>{s}</div></div>
                <div style={{width:22,height:22,borderRadius:99,border:on?"7px solid var(--brand)":"2px solid var(--line-2)"}}/>
              </div>
            ))}
          </Card>
        </div>
        {/* summary */}
        <Card>
          <MoneyRow label="Subtotal" value={F2.inr(t.sub)}/>
          <MoneyRow label="Discount" value={"− "+F2.inr(t.disc)} accent="var(--pos)"/>
          <MoneyRow label="GST" value={"+ "+F2.inr(t.gst)}/>
          <div style={{borderTop:"1px dashed var(--line-2)",margin:"4px 0"}}/>
          <MoneyRow strong label="Total payable" value={F2.inr(t.total)}/>
        </Card>
        <div style={{display:"flex",alignItems:"center",gap:8,fontSize:12.5,color:"var(--ink-3)"}}>
          <Icon name="bolt" size={14} color="var(--brand)"/> Shree Balaji Distributors · delivery within a day
        </div>
      </div>
      <div style={{position:"absolute",left:0,right:0,bottom:0,padding:"14px 16px 30px",background:"linear-gradient(to top,var(--surface) 64%,transparent)"}}>
        <Button variant="primary" size="lg" full icon="bolt">Place order · {F2.inr(t.total)}</Button>
      </div>
    </Screen>
  );
}

/* ---------------- PAYMENT ---------------- */
function SK_Payment() {
  const upi = [["GPay","#1a73e8"],["PhonePe","#5f259f"],["Paytm","#00b9f1"]];
  return (
    <div style={{height:"100%",position:"relative",overflow:"hidden",background:"rgba(0,0,0,.4)"}}>
      <div style={{paddingTop:IOS_TOP}}/>
      <div style={{position:"absolute",left:0,right:0,bottom:0,top:80,background:"var(--surface)",borderRadius:"28px 28px 0 0",overflow:"hidden",display:"flex",flexDirection:"column"}}>
        <div style={{background:"var(--brand-700)",color:"#fff",padding:"18px 20px 16px",display:"flex",alignItems:"center",gap:12}}>
          <div style={{width:40,height:40,borderRadius:12,background:"rgba(255,255,255,.15)",display:"grid",placeItems:"center"}}><Icon name="bolt" size={22} color="#fff"/></div>
          <div style={{flex:1}}><div style={{fontWeight:800,fontSize:16}}>Flash<span style={{color:"var(--gold)"}}>Mart</span></div><div style={{fontSize:12,opacity:.75}}>Secured by Razorpay</div></div>
          <div style={{textAlign:"right"}}><div className="mono" style={{fontSize:18,fontWeight:700}}>{F2.inr(3998)}</div><div style={{fontSize:11,opacity:.75}}>ORD-1126</div></div>
        </div>
        <div className="fm-scroll" style={{flex:1,overflowY:"auto",padding:"18px 16px 24px"}}>
          <SectionLabel>UPI</SectionLabel>
          <div style={{display:"flex",gap:10,marginBottom:20}}>
            {upi.map(([n,c])=>(
              <div key={n} style={{flex:1,border:"1px solid var(--line)",borderRadius:16,padding:"14px 8px",textAlign:"center",background:"var(--surface)"}}>
                <div style={{width:36,height:36,borderRadius:10,background:c,color:"#fff",display:"grid",placeItems:"center",margin:"0 auto 8px",fontWeight:800,fontSize:15}}>{n[0]}</div>
                <div style={{fontSize:12,fontWeight:700}}>{n}</div>
              </div>
            ))}
          </div>
          <SectionLabel>Other methods</SectionLabel>
          <Card pad={6}>
            {[["card","Cards","Visa · Mastercard · RuPay"],["wallet","Wallets","Amazon Pay, Mobikwik"],["doc","Net banking","All major banks"]].map(([ic,t3,s],i)=>(
              <Row key={t3} last={i===2}
                left={<div style={{width:38,height:38,borderRadius:11,background:"var(--surface-2)",color:"var(--ink-3)",display:"grid",placeItems:"center"}}><Icon name={ic} size={19}/></div>}
                title={t3} sub={s} right={<Icon name="chevR" size={17} color="var(--ink-4)"/>}/>
            ))}
          </Card>
        </div>
        <div style={{padding:"12px 16px 30px",boxShadow:"0 -1px 0 var(--line)"}}>
          <Button variant="primary" size="lg" full icon="bolt">Pay {F2.inr(3998)}</Button>
        </div>
      </div>
    </div>
  );
}

/* ---------------- SUCCESS ---------------- */
function SK_Success() {
  return (
    <div style={{height:"100%",display:"flex",flexDirection:"column",background:"var(--surface)"}}>
      <div style={{paddingTop:IOS_TOP}}/>
      <div style={{flex:1,display:"flex",flexDirection:"column",alignItems:"center",justifyContent:"center",textAlign:"center",padding:"0 30px"}}>
        <SuccessCheck size={104} color="var(--pos)"/>
        <div style={{fontSize:26,fontWeight:700,letterSpacing:"-.025em",marginTop:22}}>Order placed!</div>
        <div style={{fontSize:14.5,color:"var(--ink-3)",marginTop:8,lineHeight:1.5,maxWidth:280}}>Your dealer has been notified and will confirm shortly.</div>
        <Card style={{width:"100%",marginTop:26,textAlign:"left"}}>
          {[["Order","ORD-1126"],["Amount paid",F2.inr(3998)],["Expected","Tomorrow, by 6 PM"]].map(([l,v],i)=>(
            <div key={l} style={{display:"flex",justifyContent:"space-between",alignItems:"center",padding:"10px 0",borderBottom:i<2?"1px dashed var(--line-2)":"none"}}>
              <span style={{fontSize:13,color:"var(--ink-3)",fontWeight:600}}>{l}</span>
              <span className={i===2?"":"mono"} style={{fontSize:13.5,fontWeight:700,color:i===2?"var(--pos)":"var(--ink)"}}>{v}</span>
            </div>
          ))}
        </Card>
      </div>
      <div style={{padding:"0 24px 36px",display:"flex",flexDirection:"column",gap:11}}>
        <Button variant="primary" size="lg" full icon="truck">Track order</Button>
        <Button variant="ghost" full icon="receipt">View invoice</Button>
      </div>
    </div>
  );
}

/* ---------------- MY ORDERS ---------------- */
function SK_Orders() {
  const orders = F2.SEED_ORDERS.filter(o=>o.shop==="SHP-118"||o.shop==="SHP-092");
  return (
    <Screen nav={<NavBar items={skNav("orders")} active="orders"/>}>
      <TopBar title="My orders" subtitle="Ramesh General Store" right={<GlyphBtn name="search"/>}/>
      <div style={{padding:"0 16px 12px"}}><Segmented full value="All" onChange={()=>{}} options={["All","Pending","Delivered","Cancelled"]}/></div>
      <div style={{padding:"0 16px",display:"flex",flexDirection:"column",gap:11}}>
        {orders.map(o=>{
          const tot = F2.orderMath(o.items).total;
          return (
            <Card key={o.id} pad={14}>
              <div style={{display:"flex",alignItems:"center",gap:11,marginBottom:11}}>
                <div style={{width:40,height:40,borderRadius:12,background:"var(--surface-2)",color:"var(--ink-3)",display:"grid",placeItems:"center"}}><Icon name="bag" size={19}/></div>
                <div style={{flex:1,minWidth:0}}><div className="mono" style={{fontSize:14.5,fontWeight:700}}>{o.id}</div><div style={{fontSize:12,color:"var(--ink-4)"}}>{o.date} · {o.items.length} items</div></div>
                <Badge status={o.status==="placed"?"pending":o.status} size="sm"/>
              </div>
              <div style={{display:"flex",gap:6,flexWrap:"wrap",marginBottom:12}}>
                {o.items.slice(0,3).map(it=><span key={it.pid} className="mono" style={{fontSize:11,fontWeight:600,color:"var(--ink-3)",background:"var(--surface-2)",padding:"4px 9px",borderRadius:8}}>{it.qty}×{it.name.split(" ").slice(0,2).join(" ")}</span>)}
              </div>
              <div style={{display:"flex",alignItems:"center",justifyContent:"space-between",paddingTop:11,borderTop:"1px solid var(--line)"}}>
                <span className="mono" style={{fontSize:15,fontWeight:700}}>{F2.inr(tot)}</span>
                <div style={{display:"flex",gap:8}}>
                  {o.status==="delivered"&&<Button variant="soft" size="sm" icon="receipt">Invoice</Button>}
                  <Button variant={o.status==="delivered"?"ghost":"primary"} size="sm" icon={o.status==="delivered"?"refresh":"truck"}>{o.status==="delivered"?"Reorder":"Track"}</Button>
                </div>
              </div>
            </Card>
          );
        })}
      </div>
    </Screen>
  );
}

/* ---------------- INVOICE ---------------- */
function SK_Invoice() {
  const items=[["atta",6],["oil",12],["salt",10]].map(([id,q])=>({...F2.lookups.product(id),qty:q}));
  const t=F2.orderMath(items);
  return (
    <Screen>
      <TopBar title="Tax invoice" subtitle="INV-1124" onBack right={<GlyphBtn name="download"/>}/>
      <div style={{padding:"4px 16px 110px"}}>
        <Card pad={0} style={{overflow:"hidden"}}>
          <div style={{background:"var(--ink-surface)",color:"#fff",padding:"18px 18px 16px",display:"flex",justifyContent:"space-between",alignItems:"flex-start"}}>
            <div><div style={{fontWeight:800,fontSize:17}}>Flash<span style={{color:"var(--gold)"}}>Mart</span></div><div style={{fontSize:11,opacity:.6,marginTop:3}}>Tax Invoice · GST compliant</div></div>
            <div style={{textAlign:"right"}}><div className="mono" style={{fontSize:13,fontWeight:700}}>INV-1124</div><div style={{fontSize:11,opacity:.6,marginTop:3}}>Jun 6, 2026</div></div>
          </div>
          <div style={{display:"flex",padding:"14px 18px",gap:14,borderBottom:"1px solid var(--line)"}}>
            <div style={{flex:1}}><div style={{fontSize:10.5,color:"var(--ink-4)",fontWeight:700,textTransform:"uppercase"}}>Billed to</div><div style={{fontSize:13,fontWeight:700,marginTop:4}}>Ramesh General Store</div><div className="mono" style={{fontSize:11,color:"var(--ink-4)",marginTop:2}}>27ABCDE1234F1Z5</div></div>
            <div style={{flex:1}}><div style={{fontSize:10.5,color:"var(--ink-4)",fontWeight:700,textTransform:"uppercase"}}>Dealer</div><div style={{fontSize:13,fontWeight:700,marginTop:4}}>Shree Balaji Dist.</div><div className="mono" style={{fontSize:11,color:"var(--ink-4)",marginTop:2}}>Andheri East</div></div>
          </div>
          <div style={{padding:"8px 18px"}}>
            {items.map((it,i)=>{const m=F2.lineMath(it);return(
              <div key={it.id} style={{display:"flex",alignItems:"center",gap:10,padding:"10px 0",borderBottom:i<items.length-1?"1px solid var(--line)":"none"}}>
                <div style={{flex:1,minWidth:0}}><div style={{fontSize:13,fontWeight:600}}>{it.name}</div><div className="mono" style={{fontSize:11,color:"var(--ink-4)",marginTop:1}}>{it.qty}×{F2.inr(it.price)} · GST {it.gst}%</div></div>
                <span className="mono" style={{fontSize:13,fontWeight:700}}>{F2.inr(m.total)}</span>
              </div>
            );})}
          </div>
          <div style={{padding:"10px 18px 18px",background:"var(--surface-2)"}}>
            <MoneyRow label="Taxable value" value={F2.inr(t.sub-t.disc)}/>
            <MoneyRow label="CGST + SGST" value={F2.inr(t.gst)}/>
            <div style={{borderTop:"1px dashed var(--line-2)",margin:"4px 0"}}/>
            <MoneyRow strong label="Invoice total" value={F2.inr(t.total)} accent="var(--pos)"/>
          </div>
        </Card>
      </div>
      <div style={{position:"absolute",left:0,right:0,bottom:0,padding:"14px 16px 30px",background:"linear-gradient(to top,var(--surface) 64%,transparent)",display:"flex",gap:12}}>
        <Button variant="ghost" full icon="upload">Share</Button>
        <Button variant="primary" full icon="download">Download PDF</Button>
      </div>
    </Screen>
  );
}

Object.assign(window,{SK_Checkout,SK_Payment,SK_Success,SK_Orders,SK_Invoice});
