document.addEventListener('DOMContentLoaded', () => {
    // Tab Switching Logic
    const navBtns = document.querySelectorAll('.nav-btn');
    const tabPanes = document.querySelectorAll('.tab-pane');

    navBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            // Remove active classes
            navBtns.forEach(b => b.classList.remove('active'));
            tabPanes.forEach(p => p.classList.remove('active'));

            // Add active class to clicked
            btn.classList.add('active');
            const targetId = btn.getAttribute('data-target');
            document.getElementById(targetId).classList.add('active');
        });
    });

    // Calc Method Switching
    const methodRadios = document.querySelectorAll('input[name="calc-method"]');
    const formDgnl = document.getElementById('form-dgnl');
    const formVsat = document.getElementById('form-vsat');
    const calcResultArea = document.getElementById('calc-result-area');

    methodRadios.forEach(radio => {
        radio.addEventListener('change', (e) => {
            calcResultArea.classList.add('hidden'); // Hide result when changing method
            if (e.target.value === 'dgnl') {
                formDgnl.classList.remove('hidden');
                formVsat.classList.add('hidden');
            } else {
                formDgnl.classList.add('hidden');
                formVsat.classList.remove('hidden');
            }
        });
    });

    // Toggle password visibility
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');
    const eyeIcon = document.getElementById('eye-icon');

    if (togglePassword) {
        togglePassword.addEventListener('click', function () {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            
            if (type === 'text') {
                eyeIcon.innerHTML = '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line>';
            } else {
                eyeIcon.innerHTML = '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle>';
            }
        });
    }

    // Tra Cứu Logic (API Integration)
    const tracuuForm = document.getElementById('tracuu-form');
    const tracuuLoading = document.getElementById('tracuu-loading');
    const tracuuResult = document.getElementById('tracuu-result');

    const renderResult = (data) => {
        if (!data.success) {
            tracuuResult.innerHTML = `
                <div class="result-box error" style="border-left-color: #f59e0b;">
                    <div class="result-header" style="color: #d97706;">Không tìm thấy thông tin</div>
                    <p>${data.message || 'Không tìm thấy dữ liệu thí sinh khớp với CCCD và Ngày sinh vừa nhập.'}</p>
                </div>
                <div style="margin-top: 1rem; text-align: center;">
                    <button class="btn btn-primary" onclick="window.location.reload()">Làm mới</button>
                </div>
            `;
            return;
        }

        // ── Determine phuong thuc label ──
        const ptLabel = (pt) => {
            if (!pt) return '—';
            const p = pt.toUpperCase();
            if (p.includes('DGNL') || p === '4') return 'ĐGNL (Thang 1200)';
            if (p.includes('VSAT') || p === '5') return 'VSAT/THPT';
            if (p.includes('PT') || p === '1' || p === '2') return 'Xét điểm THPT';
            return pt;
        };

        // ── Banner summary ──
        let bannerHtml = '';
        if (data.admitted) {
            const nv = data.admittedNV;
            bannerHtml = `
                <div style="background: #f0fdf4; border: 2px solid #22c55e; border-radius: 12px; padding: 1.2rem 1.5rem; margin-bottom: 1.5rem;">
                    <div style="font-size: 1.1rem; font-weight: 700; color: #15803d; margin-bottom: 6px;">TRÚNG TUYỂN</div>
                    <div style="color: #166534;">
                        <strong>${nv ? nv.tenNganh : ''}</strong>${nv && nv.maNganh ? ' (' + nv.maNganh + ')' : ''}
                        &nbsp;—&nbsp; Nguyện vọng ${nv ? nv.thuTu : ''} &nbsp;—&nbsp; Điểm: <strong>${nv && nv.diemXetTuyen ? nv.diemXetTuyen.toFixed(2) : '—'}</strong>
                    </div>
                    <div style="font-size: 0.85rem; color: #4d7c0f; margin-top: 6px;">Vui lòng kiểm tra email và làm thủ tục nhập học.</div>
                </div>`;
        } else if (data.pending) {
            bannerHtml = `
                <div style="background: #eff6ff; border: 2px solid #3b82f6; border-radius: 12px; padding: 1.2rem 1.5rem; margin-bottom: 1.5rem;">
                    <div style="font-size: 1.1rem; font-weight: 700; color: #1d4ed8; margin-bottom: 6px;">ĐANG TRONG XÉT TUYỂN</div>
                    <div style="color: #1e40af;">Các nguyện vọng của bạn hiện đang được xét tuyển. Vui lòng chờ kết quả.</div>
                </div>`;
        } else if (data.nguyenVongs && data.nguyenVongs.length > 0) {
            bannerHtml = `
                <div style="background: #fff1f2; border: 2px solid #f43f5e; border-radius: 12px; padding: 1.2rem 1.5rem; margin-bottom: 1.5rem;">
                    <div style="font-size: 1.1rem; font-weight: 700; color: #be123c; margin-bottom: 6px;">KHÔNG TRÚNG TUYỂN</div>
                    <div style="color: #9f1239;">Rất tiếc, bạn chưa đủ điều kiện trúng tuyển vào các nguyện vọng đã đăng ký.</div>
                </div>`;
        } else {
            bannerHtml = `
                <div style="background: #f8fafc; border: 2px solid #94a3b8; border-radius: 12px; padding: 1.2rem 1.5rem; margin-bottom: 1.5rem;">
                    <div style="font-weight: 700; color: #475569;">Chưa có nguyện vọng đăng ký.</div>
                </div>`;
        }

        // ── Detail table of all aspirations ──
        let tableHtml = '';
        if (data.nguyenVongs && data.nguyenVongs.length > 0) {
            const rows = data.nguyenVongs.map(nv => {
                const kq = (nv.ketQua || '').toLowerCase();
                const isYes = kq === 'yes' || kq === 'đậu' || kq === 'trúng tuyển';
                const isPendingRow = kq === '' || kq == null;
                const isDuoiSan = kq === 'duoisan' || kq === 'rớt' || kq === 'không trúng';

                let badge = '';
                if (isYes) {
                    badge = '<span style="background:#22c55e;color:#fff;padding:3px 12px;border-radius:999px;font-size:0.8rem;font-weight:600;">Trúng tuyển</span>';
                } else if (isPendingRow) {
                    badge = '<span style="background:#3b82f6;color:#fff;padding:3px 12px;border-radius:999px;font-size:0.8rem;font-weight:600;">Đang xét</span>';
                } else if (isDuoiSan) {
                    badge = '<span style="background:#f43f5e;color:#fff;padding:3px 12px;border-radius:999px;font-size:0.8rem;font-weight:600;">Không đạt</span>';
                } else {
                    badge = `<span style="background:#94a3b8;color:#fff;padding:3px 12px;border-radius:999px;font-size:0.8rem;">${nv.ketQua || '—'}</span>`;
                }

                return `
                    <tr>
                        <td style="text-align:center;font-weight:700;padding:12px 8px;">${nv.thuTu ?? '—'}</td>
                        <td style="padding:12px 8px;"><strong>${nv.tenNganh || '—'}</strong><br><span style="font-size:0.8rem;color:#64748b;margin-top:2px;display:block;">${nv.maNganh || ''}</span></td>
                        <td style="padding:12px 8px;">${ptLabel(nv.phuongThuc)}</td>
                        <td style="text-align:center;padding:12px 8px;">${nv.toHop || '—'}</td>
                        <td style="text-align:center;font-weight:700;color:#1e3a8a;padding:12px 8px;">${nv.diemXetTuyen != null ? nv.diemXetTuyen.toFixed(2) : '—'}</td>
                        <td style="text-align:center;padding:12px 8px;">${nv.diemSan != null ? nv.diemSan.toFixed(2) : '—'}</td>
                        <td style="text-align:center;padding:12px 8px;">${nv.diemChuan != null ? nv.diemChuan.toFixed(2) : '—'}</td>
                        <td style="text-align:center;padding:12px 8px;">${badge}</td>
                    </tr>`;
            }).join('');

            tableHtml = `
                <div style="overflow-x:auto; margin-bottom: 1.5rem;">
                    <table class="result-table" style="min-width:700px; border-collapse: separate; border-spacing: 0;">
                        <thead>
                            <tr>
                                <th style="text-align:center;padding:12px 8px;">NV</th>
                                <th style="padding:12px 8px;">Ngành</th>
                                <th style="padding:12px 8px;">Phương thức</th>
                                <th style="text-align:center;padding:12px 8px;">Tổ hợp</th>
                                <th style="text-align:center;padding:12px 8px;">Điểm XT</th>
                                <th style="text-align:center;padding:12px 8px;">Điểm sàn</th>
                                <th style="text-align:center;padding:12px 8px;">Điểm chuẩn</th>
                                <th style="text-align:center;padding:12px 8px;">Kết quả</th>
                            </tr>
                        </thead>
                        <tbody>${rows}</tbody>
                    </table>
                </div>`;
        }

        // ── Refresh button ──
        const refreshBtn = `
            <div style="margin-top: 1rem; text-align: center;">
                <button class="btn btn-primary" id="btn-reload">
                    <svg style="width:16px;height:16px;display:inline-block;vertical-align:middle;margin-right:5px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
                    Làm mới để cập nhật điểm
                </button>
            </div>`;

        tracuuResult.innerHTML = bannerHtml + tableHtml + refreshBtn;
        
        const btnReload = document.getElementById('btn-reload');
        if (btnReload) {
            btnReload.addEventListener('click', (e) => {
                e.preventDefault();
                tracuuForm.dispatchEvent(new Event('submit'));
            });
        }
    };

    tracuuForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const cccd = document.getElementById('cccd').value;
        const password = document.getElementById('password').value;

        // Reset state
        tracuuForm.style.display = 'none';
        tracuuLoading.classList.remove('hidden');
        tracuuResult.classList.add('hidden');

        fetch('http://localhost:8080/api/tracuu', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ cccd, password })
        })
        .then(response => response.json())
        .then(data => {
            tracuuLoading.classList.add('hidden');
            tracuuResult.classList.remove('hidden');
            tracuuForm.style.display = 'block';
            renderResult(data);
        })
        .catch(error => {
            console.error('Error fetching data:', error);
            tracuuLoading.classList.add('hidden');
            tracuuResult.classList.remove('hidden');
            tracuuForm.style.display = 'block';
            renderResult({ success: false, message: 'Lỗi kết nối máy chủ, vui lòng thử lại sau!' });
        });
    });

    // Form Tính Điểm DGNL
    formDgnl.addEventListener('submit', (e) => {
        e.preventDefault();
        
        const nganhSelect = document.getElementById('dgnl-nganh');
        const nganhName = nganhSelect.options[nganhSelect.selectedIndex].text;
        const maNganh = nganhSelect.value;
        const diemthi = parseFloat(document.getElementById('dgnl-diemthi').value) || 0;
        const diemcong = parseFloat(document.getElementById('dgnl-diemcong').value) || 0;
        const khuvuc = parseFloat(document.getElementById('dgnl-khuvuc').value) || 0;
        const doituong = parseFloat(document.getElementById('dgnl-doituong').value) || 0;
        
        calcResultArea.classList.add('hidden');

        fetch('http://localhost:8080/api/tinhdiem/dgnl', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                phuongThuc: 'DGNL',
                maNganh: maNganh,
                diemThi: diemthi,
                diemCong: diemcong,
                khuVuc: khuvuc,
                doiTuong: doituong
            })
        })
        .then(res => res.json())
        .then(data => {
            if (!data.success) { alert('Lỗi: ' + data.message); return; }
            calcResultArea.innerHTML = `
                <div class="result-header text-red">Tính quy đổi điểm xét tuyển ĐGNL: ${data.tenNganh || nganhName}</div>
                <p style="font-size: 0.9rem; margin-bottom: 1rem;">Tổ hợp gốc quy đổi theo ngành: <span style="background: #1e3a8a; color: white; padding: 2px 6px; border-radius: 4px; font-size: 0.8rem;">${data.toHop || 'D01'}</span>, ngưỡng đầu vào: ${data.diemNguong || ''}</p>
                
                <div style="overflow-x: auto;">
                    <table class="result-table">
                        <thead>
                            <tr>
                                <th>Nội dung</th>
                                <th>Chi tiết điểm</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>Điểm thi ĐGNL</td>
                                <td>${data.diemThi.toFixed(2)}</td>
                            </tr>
                            <tr>
                                <td>Công thức quy đổi về thang 30</td>
                                <td>${data.diema != null ? `${data.diemc.toFixed(2)} + ( ${data.diemThi.toFixed(2)} - ${data.diema.toFixed(2)} ) / ( ${data.diemb.toFixed(2)} - ${data.diema.toFixed(2)} ) * ( ${data.diemd.toFixed(2)} - ${data.diemc.toFixed(2)} )` : data.diemQuyDoi.toFixed(2)}</td>
                            </tr>
                            <tr>
                                <td>Điểm thi quy đổi</td>
                                <td>${data.diemQuyDoi.toFixed(2)}</td>
                            </tr>
                            <tr>
                                <td>Điểm cộng</td>
                                <td>${data.diemCong.toFixed(2)}</td>
                            </tr>
                            <tr>
                                <td>Điểm ưu tiên (KV + ĐT)</td>
                                <td>${data.diemUuTien.toFixed(2)}</td>
                            </tr>
                            <tr>
                                <td><strong>Điểm xét tuyển</strong> = điểm thi quy đổi + điểm cộng + điểm ưu tiên quy đổi</td>
                                <td><strong>${data.diemXetTuyen.toFixed(2)}</strong></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div style="margin-top: 1rem; padding: 1rem; background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 0.5rem;">
                    <strong>Kết quả:</strong> ${data.isDat ? '<span style="color: green">ĐẠT ngưỡng</span>' : '<span style="color: red">KHÔNG ĐẠT ngưỡng</span>'}
                </div>
            `;
            calcResultArea.classList.remove('hidden');
            calcResultArea.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }).catch(err => {
            console.error(err);
            alert("Lỗi kết nối máy chủ");
        });
    });

    // Form Tính Điểm VSAT
    formVsat.addEventListener('submit', (e) => {
        e.preventDefault();
        
        const nganhSelect = document.getElementById('vsat-nganh');
        const nganhName = nganhSelect.options[nganhSelect.selectedIndex].text;
        const nganhId = nganhSelect.value;

        // Collect all subjects
        const toan = parseFloat(document.getElementById('vsat-toan').value) || 0;
        const van  = parseFloat(document.getElementById('vsat-van').value)  || 0;
        const ly   = parseFloat(document.getElementById('vsat-ly')?.value)  || 0;
        const hoa  = parseFloat(document.getElementById('vsat-hoa')?.value) || 0;
        const sinh = parseFloat(document.getElementById('vsat-sinh').value) || 0;
        const su   = parseFloat(document.getElementById('vsat-su')?.value)  || 0;
        const dia  = parseFloat(document.getElementById('vsat-dia')?.value) || 0;
        const anh  = parseFloat(document.getElementById('vsat-anh')?.value) || 0;

        const kvSelect = document.getElementById('vsat-khuvuc');
        const dtSelect = document.getElementById('vsat-doituong');
        const kv = parseFloat(kvSelect.value) || 0;
        const dt = parseFloat(dtSelect.value) || 0;
        const dc = parseFloat(document.getElementById('vsat-diemcong').value) || 0;
        const kvLabel = kvSelect.options[kvSelect.selectedIndex].text;
        const dtLabel = dtSelect.options[dtSelect.selectedIndex].text;

        // Map subject code to full name
        const subjectName = {
            TO: 'Toán', VA: 'Ngữ văn', SI: 'Sinh học',
            LI: 'Vật lý', HO: 'Hóa học', SU: 'Lịch sử',
            DI: 'Địa lý', AN: 'Tiếng Anh'
        };

        calcResultArea.classList.add('hidden');

        fetch('http://localhost:8080/api/tinhdiem/vsat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                phuongThuc: 'VSAT', maNganh: nganhId,
                diemCong: dc, khuVuc: kv, doiTuong: dt,
                toan, van, ly, hoa, sinh, su, dia, anh
            })
        })
        .then(res => res.json())
        .then(data => {
            if (!data.success) { alert('Lỗi: ' + data.message); return; }

            // ── Build combination detail rows ──
            let combinationsHtml = '';
            if (data.toHopDetails && data.toHopDetails.length > 0) {
                data.toHopDetails.forEach((th, idx) => {
                    const m1Code = th.mon1 || '';
                    const m2Code = th.mon2 || '';
                    const m3Code = th.mon3 || '';
                    const m1Val  = th.m1Val ?? 0;
                    const m2Val  = th.m2Val ?? 0;
                    const m3Val  = th.m3Val ?? 0;
                    const m1Conv = th.m1Conv ?? 0;
                    const m2Conv = th.m2Conv ?? 0;
                    const m3Conv = th.m3Conv ?? 0;
                    const hs1    = th.hs1 ?? 1;
                    const hs2    = th.hs2 ?? 1;
                    const hs3    = th.hs3 ?? 1;
                    const hsSum  = hs1 + hs2 + hs3;
                    const dolech = th.dolech ?? 0;
                    const dthxt  = th.dthxt ?? 0;
                    const xetNguong = th.xetNguong ?? 0;
                    const dut    = th.dut ?? (kv + dt);
                    const dxtCombo = th.diemXetTuyen ?? 0;

                    // Build formula string: c + ( val - a ) / ( b - a ) * ( d - c ) = conv
                    const fmtMon = (code, val, conv, error, diema, diemb, diemc, diemd) => {
                        if (error) return `Lỗi, điểm nhập vào không nằm trong phân vị nào = 0`;
                        if (val <= 0) return `0.00`;
                        if (diema == null) return `${conv.toFixed(2)}`;
                        const a = (+diema).toFixed(2), b = (+diemb).toFixed(2);
                        const c = (+diemc).toFixed(2), d = (+diemd).toFixed(2);
                        return `${c} + ( ${val} - ${a} ) / ( ${b} - ${a} ) * ( ${d} - ${c} ) = ${conv.toFixed(2)}`;
                    };

                    const dthxtFormula = `(${m1Conv.toFixed(2)} * ${hs1} + ${m2Conv.toFixed(2)} * ${hs2} + ${m3Conv.toFixed(2)} * ${hs3}) / ${hsSum} * 3`;

                    combinationsHtml += `
                        <tr style="background:#f0f4ff;">
                            <td colspan="2" style="padding:10px 12px;font-weight:700;font-size:0.95rem;">
                                ${idx + 1}) Tổ hợp: ${th.maTohop} — ${subjectName[m1Code] || m1Code}, ${subjectName[m2Code] || m2Code}, ${subjectName[m3Code] || m3Code}
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:8px 12px;">Điểm môn ${m1Code}:${m1Val}</td>
                            <td style="padding:8px 12px;">${fmtMon(m1Code, m1Val, m1Conv, th.m1Error, th.m1Diema, th.m1Diemb, th.m1Diemc, th.m1Diemd)}</td>
                        </tr>
                        <tr>
                            <td style="padding:8px 12px;">Điểm môn ${m2Code}:${m2Val}</td>
                            <td style="padding:8px 12px;">${fmtMon(m2Code, m2Val, m2Conv, th.m2Error, th.m2Diema, th.m2Diemb, th.m2Diemc, th.m2Diemd)}</td>
                        </tr>
                        <tr>
                            <td style="padding:8px 12px;">Điểm môn ${m3Code}:${m3Val}</td>
                            <td style="padding:8px 12px;">${fmtMon(m3Code, m3Val, m3Conv, th.m3Error, th.m3Diema, th.m3Diemb, th.m3Diemc, th.m3Diemd)}</td>
                        </tr>
                        <tr>
                            <td style="padding:8px 12px;">Xét ngưỡng = Tổng điểm 3 môn + điểm ưu tiên</td>
                            <td style="padding:8px 12px;">(${m1Conv.toFixed(2)} + ${m2Conv.toFixed(2)} + ${m3Conv.toFixed(2)} + ${kv} + ${dt}) = <span style="background:#fef08a;padding:2px 6px;border-radius:4px;font-weight:700;">${xetNguong.toFixed(2)}</span></td>
                        </tr>
                        <tr>
                            <td style="padding:8px 12px;">Điểm tổ hợp xét tuyển (DTHXT) = (${m1Code} * ${hs1} + ${m2Code} * ${hs2} + ${m3Code} * ${hs3}) / ${hsSum} * 3</td>
                            <td style="padding:8px 12px;">${dthxtFormula} = <span style="background:#bae6fd;padding:2px 6px;border-radius:4px;font-weight:700;">${dthxt.toFixed(3)}</span></td>
                        </tr>
                        <tr>
                            <td style="padding:8px 12px;">Độ lệch với TH Gốc (D01)</td>
                            <td style="padding:8px 12px;">${dolech}</td>
                        </tr>
                        <tr>
                            <td style="padding:8px 12px;">Điểm ưu tiên (DUT)</td>
                            <td style="padding:8px 12px;">${dut.toFixed(2)}</td>
                        </tr>
                        <tr>
                            <td style="padding:8px 12px;font-weight:600;">Điểm xét tuyển (DXT) = DTHXT + DC + DUT − Độ lệch (≤30)</td>
                            <td style="padding:8px 12px;">${dthxt.toFixed(3)} + ${dc} + ${dut.toFixed(2)} − ${dolech} ⇒ <span style="background:#fecaca;color:#b91c1c;padding:2px 6px;border-radius:4px;font-weight:700;">${dxtCombo.toFixed(2)}</span></td>
                        </tr>`;
                });
            }

            calcResultArea.innerHTML = `
                <div class="result-header text-red">Tính điểm vào ngành xét tuyển: <strong>${data.tenNganh || nganhName}</strong> (${nganhId})</div>
                <p style="font-size:0.9rem;margin-bottom:0.5rem;">
                    Tổ hợp có điểm xét tuyển cao nhất: <span class="text-red"><strong>${data.toHop || 'N/A'}, ${data.diemXetTuyen.toFixed(2)} điểm.</strong></span>
                    Tổ hợp gốc: <span style="background:#1e3a8a;color:white;padding:2px 8px;border-radius:4px;font-size:0.8rem;font-weight:600;">${data.toHop || 'D01'}</span>
                    ngưỡng đầu vào: <strong>${data.diemNguong != null ? data.diemNguong : '—'}</strong>
                </p>
                <p style="font-size:0.85rem;color:#3b82f6;margin-bottom:1.2rem;cursor:pointer;" onclick="calcResultArea.classList.add('hidden')">← Quay lại</p>

                <div style="overflow-x:auto;">
                    <table class="result-table" style="min-width:680px;">
                        <thead>
                            <tr>
                                <th style="width:55%;padding:10px 12px;">Nội dung</th>
                                <th style="padding:10px 12px;">Chi tiết điểm</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td style="padding:8px 12px;">Khu vực ưu tiên: ${kvLabel}</td>
                                <td style="padding:8px 12px;">${kv.toFixed(2)}</td>
                            </tr>
                            <tr>
                                <td style="padding:8px 12px;">Đối tượng ưu tiên: ${dtLabel}</td>
                                <td style="padding:8px 12px;">${dt.toFixed(2)}</td>
                            </tr>
                            <tr>
                                <td style="padding:8px 12px;">Điểm cộng (≤3)</td>
                                <td style="padding:8px 12px;">${dc.toFixed(2)}</td>
                            </tr>
                            ${combinationsHtml}
                        </tbody>
                    </table>
                </div>
                <div style="margin-top:1rem;padding:1rem;background:#f0fdf4;border:1px solid #bbf7d0;border-radius:0.5rem;">
                    <strong>Kết quả:</strong> ${data.isDat
                        ? '<span style="color:green;font-weight:700;">ĐẠT ngưỡng điểm sàn</span>'
                        : '<span style="color:red;font-weight:700;">KHÔNG ĐẠT ngưỡng điểm sàn</span>'}
                </div>
            `;
            calcResultArea.classList.remove('hidden');
            calcResultArea.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }).catch(err => {
            console.error(err);
            alert("Lỗi kết nối máy chủ");
        });
    });

    // Populate Nganh options on load
    function loadNganhData() {
        const dgnlSelect = document.getElementById('dgnl-nganh');
        const vsatSelect = document.getElementById('vsat-nganh');
        
        fetch('http://localhost:8080/api/nganh')
            .then(res => {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(data => {
                if (!data || data.length === 0) throw new Error('empty');
                
                let optionsHtml = '';
                data.forEach(n => {
                    // Jackson serializes field 'manganh' and 'tennganh' directly
                    const ma = n.manganh || n.maNganh || '';
                    const ten = n.tennganh || n.tenNganh || ma;
                    optionsHtml += `<option value="${ma}">${ten}</option>`;
                });
                
                if (dgnlSelect) dgnlSelect.innerHTML = optionsHtml;
                if (vsatSelect) vsatSelect.innerHTML = optionsHtml;
            })
            .catch(err => {
                console.warn('Không thể tải danh sách ngành từ server:', err);
                // Fallback: load static options so the form still works
                const fallback = [
                    '<option value="">-- Chọn ngành --</option>',
                    '<option value="7140201">Quản lý giáo dục</option>',
                    '<option value="7480201">Công nghệ thông tin</option>',
                    '<option value="7140209">Giáo dục mầm non</option>',
                    '<option value="7140202">Giáo dục tiểu học</option>',
                ].join('');
                if (dgnlSelect) dgnlSelect.innerHTML = fallback;
                if (vsatSelect) vsatSelect.innerHTML = fallback;
            });
    }
    
    loadNganhData();
});
