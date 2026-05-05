package com.internship.tool.config;

import com.internship.tool.entity.ContinuityPlan;
import com.internship.tool.repository.ContinuityPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ContinuityPlanRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        // 30 realistic Business Continuity Plans spanning 7 departments
        save("IT Disaster Recovery Plan",
                "Full DR plan covering datacenter failover, RTO within 4 hours, tested quarterly.",
                "Active", "High", "Alice Johnson", "IT", 4, 1, 88);

        save("Server Backup & Restore",
                "Daily automated backups to off-site cloud storage with weekly restore tests.",
                "Active", "High", "Bob Smith", "IT", 2, 1, 92);

        save("Network Failure Failover",
                "Automatic ISP failover to 4G/LTE backup link. Covers all critical systems.",
                "Active", "High", "Carol White", "IT", 1, 0, 85);

        save("Cybersecurity Incident Response",
                "Incident classification, containment, eradication, and recovery procedures.",
                "Active", "High", "David Brown", "IT", 8, 4, 79);

        save("Cloud Migration Contingency",
                "Rollback procedures if cloud migration causes service disruption.",
                "Pending", "Medium", "Eve Davis", "IT", 12, 6, 55);

        save("Power Outage Response",
                "UPS coverage for critical systems, generator auto-start procedure, utility vendor SLA.",
                "Active", "High", "Frank Miller", "Facilities", 0, 0, 95);

        save("Building Evacuation Plan",
                "Emergency evacuation routes, fire warden assignments, assembly point map.",
                "Active", "Medium", "Grace Wilson", "Facilities", 0, 0, 98);

        save("HVAC Failure Protocol",
                "Procedures for server room cooling failure; portable units on standby.",
                "Pending", "Medium", "Henry Moore", "Facilities", 2, 0, 62);

        save("Supply Chain Disruption Response",
                "Alternate vendor list for critical parts; 30-day safety stock policy.",
                "Active", "High", "Iris Taylor", "Operations", 24, 0, 71);

        save("Key Personnel Absence Plan",
                "Succession matrix for critical roles; cross-training schedule for top 10 positions.",
                "Active", "Medium", "Jack Anderson", "HR", 8, 0, 77);

        save("Remote Work Continuity",
                "VPN capacity, laptop provisioning, secure access for 100% remote workforce.",
                "Active", "Medium", "Karen Thomas", "IT", 4, 2, 83);

        save("Payroll Processing Continuity",
                "Manual payroll backup procedures if HRIS is unavailable for more than 24 hours.",
                "Pending", "High", "Leo Jackson", "Finance", 24, 0, 60);

        save("Financial Reporting Backup",
                "Offline ledger backup, manual journal entry procedures during system outage.",
                "Active", "High", "Mia Harris", "Finance", 48, 24, 74);

        save("Banking & Treasury Continuity",
                "Secondary banking authorisations; offline payment instruction procedures.",
                "Active", "High", "Noah Martin", "Finance", 4, 2, 81);

        save("Customer Data Breach Response",
                "GDPR breach notification workflow; regulator contact list; PR holding statements.",
                "Active", "High", "Olivia Garcia", "Legal", 2, 0, 87);

        save("Regulatory Compliance Continuity",
                "Maintains compliance reporting even during system outages via manual fallbacks.",
                "Pending", "Medium", "Peter Martinez", "Legal", 72, 24, 58);

        save("Contract Management Backup",
                "Offline access to critical contracts; emergency signing authority matrix.",
                "Active", "Low", "Quinn Robinson", "Legal", 48, 0, 70);

        save("Call Centre Failover",
                "Reroute inbound calls to BCP site or remote agents within 30 minutes.",
                "Active", "High", "Rachel Clark", "Operations", 1, 0, 90);

        save("Website Outage Response",
                "CDN failover, static maintenance page, social media communication plan.",
                "Failed", "High", "Sam Rodriguez", "IT", 2, 0, 42);

        save("ERP System Outage",
                "Manual order processing procedures; paper-based stock management fallback.",
                "Pending", "High", "Tina Lewis", "Operations", 8, 4, 65);

        save("Manufacturing Line Stoppage",
                "Alternate production scheduling; approved subcontractor list for overflow.",
                "Active", "Medium", "Uma Lee", "Operations", 4, 0, 76);

        save("Logistics & Delivery Disruption",
                "Alternate courier contracts; customer communication templates for delays.",
                "Active", "Medium", "Victor Walker", "Operations", 24, 0, 72);

        save("Staff Communication Plan",
                "Mass notification via SMS, email, and intranet during any BCP invocation.",
                "Active", "Medium", "Wendy Hall", "HR", 1, 0, 89);

        save("Talent Retention Under Crisis",
                "Retention bonuses, welfare support, and communication strategy during prolonged incidents.",
                "Under Review", "Low", "Xavier Allen", "HR", 0, 0, 55);

        save("Office Relocation Plan",
                "Alternate office site identified; IT infrastructure pre-configured; access passes ready.",
                "Active", "Medium", "Yvonne Young", "Facilities", 4, 0, 80);

        save("Insurance Claims Procedure",
                "Business interruption insurance activation; loss adjuster contacts; evidence preservation.",
                "Active", "Low", "Zach King", "Finance", 48, 0, 68);

        save("Media & PR Crisis Plan",
                "Approved spokesperson; pre-drafted holding statements; social media monitoring.",
                "Under Review", "Medium", "Amy Scott", "Management", 1, 0, 63);

        save("Executive Decision-Making Protocol",
                "BCP governance; crisis management team activation; escalation thresholds.",
                "Active", "High", "Brian Green", "Management", 0, 0, 91);

        save("Third-Party Vendor Failure",
                "Identifies single points of failure in vendor relationships; contingency contracts in place.",
                "Failed", "High", "Clara Adams", "Operations", 48, 24, 38);

        save("Pandemic / Epidemic Response",
                "Remote work triggers, hygiene protocols, reduced-capacity site operations.",
                "Active", "Medium", "Dan Baker", "HR", 24, 0, 84);
    }

    private void save(String title, String description, String status,
                      String priority, String owner, String department,
                      Integer rto, Integer rpo, Integer score) {
        repository.save(new ContinuityPlan(
                title, description, status, priority, owner, department, rto, rpo, score));
    }
}
